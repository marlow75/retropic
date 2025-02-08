package pl.dido.image.utils;

/**
 * Fixed sized (non resizable) bitvector. Upon instance construction a bitvector
 * is told to hold a fixed number of bits - it's size. The size can be any
 * number (need not be a power of 2 or so). The bits of a <tt>BitVector</tt> are
 * indexed by nonnegative integers. Any attempt to access a bit at an
 * <tt>index&lt;0 || index&gt;=size()</tt> will throw an
 * <tt>IndexOutOfBoundsException</tt>.
 * <p>
 * Individual indexed bits can be examined, set, or cleared. Subranges can
 * quickly be extracted, copied and replaced. Quick iteration over subranges is
 * provided by optimized internal iterators (<tt>forEach()</tt> methods). One
 * <code>BitVector</code> may be used to modify the contents of another
 * <code>BitVector</code> through logical AND, OR, XOR and other similar
 * operations.
 * <p>
 * All operations consider the bits <tt>0..size()-1</tt> and nothing else.
 * Operations involving two bitvectors (like AND, OR, XOR, etc.) will throw an
 * <tt>IllegalArgumentException</tt> if the secondary bit vector has a size
 * smaller than the receiver.
 * <p>
 * A <tt>BitVector</tt> is never automatically resized, but it can manually be
 * grown or shrinked via <tt>setSize(...)</tt>.
 * <p>
 * For use cases that need to store several bits per information entity, quick
 * accessors are provided that interpret subranges as 64 bit <tt>long</tt>
 * integers.
 * <p>
 * Why this class? Fist, <tt>boolean[]</tt> take one byte per stored bit. This
 * class takes one bit per stored bit. Second, many applications find the
 * semantics of <tt>java.util.BitSet</tt> not particularly helpful for their
 * needs. Third, operations working on all bits of a bitvector are extremely
 * quick. For example, on NT, Pentium Pro 200 Mhz, SunJDK1.2.2, java -classic,
 * for two bitvectors A,B (both much larger than processor cache), the following
 * results are obtained.
 * <ul>
 * <li><tt>A.and(B)</tt> i.e. A = A & B --> runs at about 35 MB/sec
 * <li><tt>A.cardinality()</tt>, i.e. determining the selectivity, the number of
 * bits in state "true" --> runs at about 80 MB/sec
 * <li>Similar performance for
 * <tt>or, xor, andNot, not, copy, replace, partFromTo, indexOf, clear</tt> etc.
 * </ul>
 * If you need extremely quick access to individual bits: Although getting and
 * setting individual bits with methods <tt>get(...)</tt>, <tt>set(...)</tt> and
 * <tt>put(...)</tt>is quick, it is even quicker (<b>but not safe</b>) to use
 * <tt>getQuick(...)</tt> and <tt>putQuick(...)</tt> or even
 * <tt>QuickBitVector</tt>.
 * <p>
 * <b>Note</b> that this implementation is not synchronized.
 *
 * @author wolfgang.hoschek@cern.ch
 * @version 1.01, 11/10/99
 * @see QuickBitVector
 * @see BitMatrix
 * @see java.util.BitSet
 */
public class BitVector {
	/*
	 * Bits are packed into arrays of "units." Currently a unit is a long, which
	 * consists of 64 bits, requiring 6 address bits. The choice of unit is
	 * determined purely by performance concerns.
	 */

	/**
	 * The bits of this object. The ith bit is stored in bits[i/64] at bit position
	 * i % 64 (where bit position 0 refers to the least significant bit and 63
	 * refers to the most significant bit).
	 *
	 * @serial
	 */
	protected long bits[];

	protected int nbits; // the size

	/**
	 * You normally need not use this method. Use this method only if performance is
	 * critical. Constructs a bit vector with the given backing bits and size.
	 * <b>WARNING:</b> For efficiency reasons and to keep memory usage low, <b>the
	 * array is not copied</b>. So if subsequently you modify the specified array
	 * directly via the [] operator, be sure you know what you're doing.
	 *
	 * <p>
	 * A bitvector is modelled as a long array, i.e. <tt>long[] bits</tt> holds bits
	 * of a bitvector. Each long value holds 64 bits. The i-th bit is stored in
	 * bits[i/64] at bit position i % 64 (where bit position 0 refers to the least
	 * significant bit and 63 refers to the most significant bit).
	 *
	 * @throws IllegalArgumentException if
	 *                                  <tt>size &lt; 0 || size &gt; bits.length*64</tt>.
	 */
	public BitVector(long[] bits, int size) {
		elements(bits, size);
	}

	/**
	 * Constructs a bit vector that holds <tt>size</tt> bits. All bits are initially
	 * <tt>false</tt>.
	 *
	 * @param size the number of bits the bit vector shall have.
	 * @throws IllegalArgumentException if <tt>size &lt; 0</tt>.
	 */
	public BitVector(int size) {
		this(QuickBitVector.makeBitVector(size, 1), size);
	}

	/**
	 * Performs a logical <b>AND</b> of the receiver with another bit vector (A = A
	 * & B). The receiver is modified so that a bit in it has the value
	 * <code>true</code> if and only if it already had the value <code>true</code>
	 * and the corresponding bit in the other bit vector argument has the value
	 * <code>true</code>.
	 *
	 * @param other a bit vector.
	 * @throws IllegalArgumentException if <tt>size() &gt; other.size()</tt>.
	 */
	public void and(BitVector other) {
		if (this == other)
			return;
		checkSize(other);
		final long[] theBits = this.bits; // cached for speed.
		final long[] otherBits = other.bits; // cached for speed.
		for (int i = theBits.length; --i >= 0;)
			theBits[i] &= otherBits[i];
	}

	/**
	 * Clears all of the bits in receiver whose corresponding bit is set in the
	 * other bitvector (A = A \ B). In other words, determines the difference
	 * (A=A\B) between two bitvectors.
	 *
	 * @param other a bitvector with which to mask the receiver.
	 * @throws IllegalArgumentException if <tt>size() &gt; other.size()</tt>.
	 */
	public void andNot(BitVector other) {
		checkSize(other);
		final long[] theBits = this.bits; // cached for speed.
		final long[] otherBits = other.bits; // cached for speed.
		for (int i = theBits.length; --i >= 0;)
			theBits[i] &= ~otherBits[i];
	}

	/**
	 * Returns the number of bits currently in the <tt>true</tt> state. Optimized
	 * for speed. Particularly quick if the receiver is either sparse or dense.
	 */
	public int cardinality() {
		int cardinality = 0;
		int fullUnits = numberOfFullUnits();
		final int bitsPerUnit = QuickBitVector.BITS_PER_UNIT;

		// determine cardinality on full units
		final long[] theBits = bits;
		for (int i = fullUnits; --i >= 0;) {
			long val = theBits[i];
			if (val == -1L) { // all bits set?
				cardinality += bitsPerUnit;
			} else if (val != 0L) { // more than one bit set?
				for (int j = bitsPerUnit; --j >= 0;) {
					if ((val & (1L << j)) != 0)
						cardinality++;
				}
			}
		}

		// determine cardinality on remaining partial unit, if any.
		for (int j = numberOfBitsInPartialUnit(); --j >= 0;) {
			if ((theBits[fullUnits] & (1L << j)) != 0)
				cardinality++;
		}

		return cardinality;
	}

	/**
	 * Checks if the given range is within the contained array's bounds.
	 */
	protected static void checkRangeFromTo(int from, int to, int theSize) {
		if (from < 0 || from > to || to >= theSize)
			throw new IndexOutOfBoundsException("from: " + from + ", to: " + to + ", size=" + theSize);
	}

	/**
	 * Sanity check for operations requiring another bitvector with at least the
	 * same size.
	 */
	protected void checkSize(BitVector other) {
		if (nbits > other.size())
			throw new IllegalArgumentException("Incompatible sizes: size=" + nbits + ", other.size()=" + other.size());
	}

	/**
	 * Clears all bits of the receiver.
	 */
	public void clear() {
		final long[] theBits = this.bits;
		for (int i = theBits.length; --i >= 0;)
			theBits[i] = 0L;

		// new LongArrayList(bits).fillFromToWith(0,size()-1,0L);
	}

	/**
	 * Changes the bit with index <tt>bitIndex</tt> to the "clear" (<tt>false</tt>)
	 * state.
	 *
	 * @param bitIndex the index of the bit to be cleared.
	 * @throws IndexOutOfBoundsException if
	 *                                   <tt>bitIndex&lt;0 || bitIndex&gt;=size()</tt>
	 */
	public void clear(int bitIndex) {
		if (bitIndex < 0 || bitIndex >= nbits)
			throw new IndexOutOfBoundsException(String.valueOf(bitIndex));
		QuickBitVector.clear(bits, bitIndex);
	}

	/**
	 * You normally need not use this method. Use this method only if performance is
	 * critical. Returns the bit vector's backing bits. <b>WARNING:</b> For
	 * efficiency reasons and to keep memory usage low, <b>the array is not
	 * copied</b>. So if subsequently you modify the returned array directly via the
	 * [] operator, be sure you know what you're doing.
	 *
	 * <p>
	 * A bitvector is modelled as a long array, i.e. <tt>long[] bits</tt> holds bits
	 * of a bitvector. Each long value holds 64 bits. The i-th bit is stored in
	 * bits[i/64] at bit position i % 64 (where bit position 0 refers to the least
	 * significant bit and 63 refers to the most significant bit).
	 */
	public long[] elements() {
		return bits;
	}

	/**
	 * You normally need not use this method. Use this method only if performance is
	 * critical. Sets the bit vector's backing bits and size. <b>WARNING:</b> For
	 * efficiency reasons and to keep memory usage low, <b>the array is not
	 * copied</b>. So if subsequently you modify the specified array directly via
	 * the [] operator, be sure you know what you're doing.
	 *
	 * <p>
	 * A bitvector is modelled as a long array, i.e. <tt>long[] bits</tt> holds bits
	 * of a bitvector. Each long value holds 64 bits. The i-th bit is stored in
	 * bits[i/64] at bit position i % 64 (where bit position 0 refers to the least
	 * significant bit and 63 refers to the most significant bit).
	 *
	 * @param bits the backing bits of the bit vector.
	 * @param size the number of bits the bit vector shall hold.
	 * @throws IllegalArgumentException if
	 *                                  <tt>size &lt; 0 || size &gt; bits.length*64</tt>.
	 */
	public void elements(long[] bits, int size) {
		if (size < 0 || size > bits.length * QuickBitVector.BITS_PER_UNIT)
			throw new IllegalArgumentException();
		this.bits = bits;
		this.nbits = size;
	}

	/**
	 * Compares this object against the specified object. The result is
	 * <code>true</code> if and only if the argument is not <code>null</code> and is
	 * a <code>BitVector</code> object that has the same size as the receiver and
	 * the same bits set to <code>true</code> as the receiver. That is, for every
	 * nonnegative <code>int</code> index <code>k</code>,
	 * 
	 * <pre>
	 * ((BitVector) obj).get(k) == this.get(k)
	 * </pre>
	 * 
	 * must be true.
	 *
	 * @param obj the object to compare with.
	 * @return <code>true</code> if the objects are the same; <code>false</code>
	 *         otherwise.
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof BitVector))
			return false;
		if (this == obj)
			return true;

		BitVector other = (BitVector) obj;
		if (size() != other.size())
			return false;

		int fullUnits = numberOfFullUnits();
		// perform logical comparison on full units
		for (int i = fullUnits; --i >= 0;)
			if (bits[i] != other.bits[i])
				return false;

		// perform logical comparison on remaining bits
		int i = fullUnits * QuickBitVector.BITS_PER_UNIT;
		for (int times = numberOfBitsInPartialUnit(); --times >= 0;) {
			if (get(i) != other.get(i))
				return false;
			i++;
		}

		return true;
	}

	/**
	 * Returns from the bitvector the value of the bit with the specified index. The
	 * value is <tt>true</tt> if the bit with the index <tt>bitIndex</tt> is
	 * currently set; otherwise, returns <tt>false</tt>.
	 *
	 * @param bitIndex the bit index.
	 * @return the value of the bit with the specified index.
	 * @throws IndexOutOfBoundsException if
	 *                                   <tt>bitIndex&lt;0 || bitIndex&gt;=size()</tt>
	 */
	public boolean get(int bitIndex) {
		if (bitIndex < 0 || bitIndex >= nbits)
			throw new IndexOutOfBoundsException(String.valueOf(bitIndex));
		return QuickBitVector.get(bits, bitIndex);
	}

	/**
	 * Returns a long value representing bits of the receiver from index
	 * <tt>from</tt> to index <tt>to</tt>. Bits are returned as a long value with
	 * the return value having bit 0 set to bit <code>from</code>, ..., bit
	 * <code>to-from</code> set to bit <code>to</code>. All other bits of the return
	 * value are set to 0. If <tt>to-from+1==0</tt> then returns zero (<tt>0L</tt>).
	 * 
	 * @param from index of start bit (inclusive).
	 * @param to   index of end bit (inclusive).
	 * @return the specified bits as long value.
	 * @throws IndexOutOfBoundsException if
	 *                                   <tt>from&lt;0 || from&gt;=size() || to&lt;0 || to&gt;=size() || to-from+1<0 || to-from+1>64</tt>
	 */
	public long getLongFromTo(int from, int to) {
		int width = to - from + 1;
		if (width == 0)
			return 0L;
		if (from < 0 || from >= nbits || to < 0 || to >= nbits || width < 0 || width > QuickBitVector.BITS_PER_UNIT)
			throw new IndexOutOfBoundsException("from:" + from + ", to:" + to);
		return QuickBitVector.getLongFromTo(bits, from, to);
	}

	/**
	 * Returns from the bitvector the value of the bit with the specified index;
	 * <b>WARNING:</b> Does not check preconditions. The value is <tt>true</tt> if
	 * the bit with the index <tt>bitIndex</tt> is currently set; otherwise, returns
	 * <tt>false</tt>.
	 *
	 * <p>
	 * Provided with invalid parameters this method may return invalid values
	 * without throwing any exception. <b>You should only use this method when you
	 * are absolutely sure that the index is within bounds.</b> Precondition
	 * (unchecked): <tt>bitIndex &gt;= 0 && bitIndex &lt; size()</tt>.
	 *
	 * @param bitIndex the bit index.
	 * @return the value of the bit with the specified index.
	 */
	public boolean getQuick(int bitIndex) {
		return QuickBitVector.get(bits, bitIndex);
	}

	/**
	 * Returns a hash code value for the receiver. The hash code depends only on
	 * which bits have been set within the receiver. The algorithm used to compute
	 * it may be described as follows.
	 * <p>
	 * Suppose the bits in the receiver were to be stored in an array of
	 * <code>long</code> integers called, say, <code>bits</code>, in such a manner
	 * that bit <code>k</code> is set in the receiver (for nonnegative values of
	 * <code>k</code>) if and only if the expression
	 * 
	 * <pre>
	 * ((k &gt;&gt; 6) &lt; bits.length) && ((bits[k &gt;&gt; 6] & (1L &lt;&lt; (bit & 0x3F))) != 0)
	 * </pre>
	 * 
	 * is true. Then the following definition of the <code>hashCode</code> method
	 * would be a correct implementation of the actual algorithm:
	 * 
	 * <pre>
	 * public int hashCode() {
	 * 	long h = 1234;
	 * 	for (int i = bits.length; --i &gt;= 0;) {
	 * 		h ^= bits[i] * (i + 1);
	 * 	}
	 * 	return (int) ((h &gt;&gt; 32) ^ h);
	 * }
	 * </pre>
	 * 
	 * Note that the hash code values change if the set of bits is altered.
	 *
	 * @return a hash code value for the receiver.
	 */
	public int hashCode() {
		long h = 1234;
		for (int i = bits.length; --i >= 0;)
			h ^= bits[i] * (i + 1);

		return (int) ((h >> 32) ^ h);
	}

	/**
	 * Performs a logical <b>NOT</b> on the bits of the receiver (A = ~A).
	 */
	public void not() {
		final long[] theBits = this.bits;
		for (int i = theBits.length; --i >= 0;)
			theBits[i] = ~theBits[i];
	}

	/**
	 * Returns the number of bits used in the trailing PARTIAL unit. Returns zero if
	 * there is no such trailing partial unit.
	 */
	protected int numberOfBitsInPartialUnit() {
		return QuickBitVector.offset(nbits);
	}

	/**
	 * Returns the number of units that are FULL (not PARTIAL).
	 */
	protected int numberOfFullUnits() {
		return QuickBitVector.unit(nbits);
	}

	/**
	 * Performs a logical <b>OR</b> of the receiver with another bit vector (A = A |
	 * B). The receiver is modified so that a bit in it has the value
	 * <code>true</code> if and only if it either already had the value
	 * <code>true</code> or the corresponding bit in the other bit vector argument
	 * has the value <code>true</code>.
	 *
	 * @param other a bit vector.
	 * @throws IllegalArgumentException if <tt>size() &gt; other.size()</tt>.
	 */
	public void or(BitVector other) {
		if (this == other)
			return;
		checkSize(other);
		final long[] theBits = this.bits; // cached for speed.
		final long[] otherBits = other.bits; // cached for speed.
		for (int i = theBits.length; --i >= 0;)
			theBits[i] |= otherBits[i];
	}

	/**
	 * Sets the bit with index <tt>bitIndex</tt> to the state specified by
	 * <tt>value</tt>.
	 *
	 * @param bitIndex the index of the bit to be changed.
	 * @param value    the value to be stored in the bit.
	 * @throws IndexOutOfBoundsException if
	 *                                   <tt>bitIndex&lt;0 || bitIndex&gt;=size()</tt>
	 */
	public void put(int bitIndex, boolean value) {
		if (bitIndex < 0 || bitIndex >= nbits)
			throw new IndexOutOfBoundsException(String.valueOf(bitIndex));
		if (value)
			QuickBitVector.set(bits, bitIndex);
		else
			QuickBitVector.clear(bits, bitIndex);
	}

	/**
	 * Sets bits of the receiver from index <code>from</code> to index
	 * <code>to</code> to the bits of <code>value</code>. Bit <code>from</code> is
	 * set to bit 0 of <code>value</code>, ..., bit <code>to</code> is set to bit
	 * <code>to-from</code> of <code>value</code>. All other bits stay unaffected.
	 * If <tt>to-from+1==0</tt> then does nothing.
	 * 
	 * @param value the value to be copied into the receiver.
	 * @param from  index of start bit (inclusive).
	 * @param to    index of end bit (inclusive).
	 * @throws IndexOutOfBoundsException if
	 *                                   <tt>from&lt;0 || from&gt;=size() || to&lt;0 || to&gt;=size() || to-from+1<0 || to-from+1>64</tt>.
	 */
	public void putLongFromTo(long value, int from, int to) {
		int width = to - from + 1;
		if (width == 0)
			return;
		if (from < 0 || from >= nbits || to < 0 || to >= nbits || width < 0 || width > QuickBitVector.BITS_PER_UNIT)
			throw new IndexOutOfBoundsException("from:" + from + ", to:" + to);
		QuickBitVector.putLongFromTo(bits, value, from, to);
	}

	/**
	 * Sets the bit with index <tt>bitIndex</tt> to the state specified by
	 * <tt>value</tt>; <b>WARNING:</b> Does not check preconditions.
	 *
	 * <p>
	 * Provided with invalid parameters this method may set invalid values without
	 * throwing any exception. <b>You should only use this method when you are
	 * absolutely sure that the index is within bounds.</b> Precondition
	 * (unchecked): <tt>bitIndex &gt;= 0 && bitIndex &lt; size()</tt>.
	 *
	 * @param bitIndex the index of the bit to be changed.
	 * @param value    the value to be stored in the bit.
	 */
	public void putQuick(int bitIndex, boolean value) {
		if (value)
			QuickBitVector.set(bits, bitIndex);
		else
			QuickBitVector.clear(bits, bitIndex);
	}

	/**
	 * Sets the bits in the given range to the state specified by <tt>value</tt>.
	 * <p>
	 * Optimized for speed. Preliminary performance (200Mhz Pentium Pro, JDK 1.2,
	 * NT): replace 10^6 ill aligned bits --> 0.002 seconds elapsed time.
	 *
	 * @param from  the start index, inclusive.
	 * @param to    the end index, inclusive.
	 * @param value the value to be stored in the bits of the range.
	 * @throws IndexOutOfBoundsException if
	 *                                   <tt>size()&gt;0 && (from&lt;0 || from&gt;to || to&gt;=size())</tt>.
	 */
	public void replaceFromToWith(int from, int to, boolean value) {
		if (nbits == 0 || to == from - 1)
			return;
		checkRangeFromTo(from, to, nbits);
		final long[] theBits = this.bits; // cached for speed

		int fromUnit = QuickBitVector.unit(from);
		int fromOffset = QuickBitVector.offset(from);
		int toUnit = QuickBitVector.unit(to);
		int toOffset = QuickBitVector.offset(to);
		int bitsPerUnit = QuickBitVector.BITS_PER_UNIT;

		long filler;
		if (value)
			filler = ~0L;
		else
			filler = 0L;

		int bitIndex = from;
		if (fromUnit == toUnit) { // only one unit to do
			QuickBitVector.putLongFromTo(theBits, filler, bitIndex, bitIndex + to - from);
			// slower: for (; bitIndex<=to; ) QuickBitVector.put(theBits,bitIndex++,value);
			return;
		}

		// treat leading partial unit, if any.
		if (fromOffset > 0) { // fix by Olivier Janssens
			QuickBitVector.putLongFromTo(theBits, filler, bitIndex, bitIndex + bitsPerUnit - fromOffset);
			bitIndex += bitsPerUnit - fromOffset + 1;
			/*
			 * slower: for (int i=bitsPerUnit-fromOffset; --i >= 0; ) {
			 * QuickBitVector.put(theBits,bitIndex++,value); }
			 */
			fromUnit++;
		}
		if (toOffset < bitsPerUnit - 1)
			toUnit--; // there is a trailing partial unit

		// treat full units, if any.
		for (int i = fromUnit; i <= toUnit;)
			theBits[i++] = filler;
		if (fromUnit <= toUnit)
			bitIndex += (toUnit - fromUnit + 1) * bitsPerUnit;

		// treat trailing partial unit, if any.
		if (toOffset < bitsPerUnit - 1) {
			QuickBitVector.putLongFromTo(theBits, filler, bitIndex, to);
			/*
			 * slower: for (int i=toOffset+1; --i >= 0; ) {
			 * QuickBitVector.put(theBits,bitIndex++,value); }
			 */
		}
	}

	/**
	 * Changes the bit with index <tt>bitIndex</tt> to the "set" (<tt>true</tt>)
	 * state.
	 *
	 * @param bitIndex the index of the bit to be set.
	 * @throws IndexOutOfBoundsException if
	 *                                   <tt>bitIndex&lt;0 || bitIndex&gt;=size()</tt>
	 */
	public void set(int bitIndex) {
		if (bitIndex < 0 || bitIndex >= nbits)
			throw new IndexOutOfBoundsException(String.valueOf(bitIndex));
		QuickBitVector.set(bits, bitIndex);
	}

	/**
	 * Returns the size of the receiver.
	 */
	public int size() {
		return nbits;
	}

	/**
	 * Returns a string representation of the receiver. For every index for which
	 * the receiver contains a bit in the "set" (<tt>true</tt>) state, the decimal
	 * representation of that index is included in the result. Such indeces are
	 * listed in order from lowest to highest, separated by ",&nbsp;" (a comma and a
	 * space) and surrounded by braces.
	 *
	 * @return a string representation of this bit vector.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer(nbits);
		String separator = "";
		buffer.append('{');

		for (int i = 0; i < nbits; i++) {
			if (get(i)) {
				buffer.append(separator);
				separator = ", ";
				buffer.append(i);
			}
		}

		buffer.append('}');
		return buffer.toString();
	}

	/**
	 * Performs a logical <b>XOR</b> of the receiver with another bit vector (A = A
	 * ^ B). The receiver is modified so that a bit in it has the value
	 * <code>true</code> if and only if one of the following statements holds:
	 * <ul>
	 * <li>The bit initially has the value <code>true</code>, and the corresponding
	 * bit in the argument has the value <code>false</code>.
	 * <li>The bit initially has the value <code>false</code>, and the corresponding
	 * bit in the argument has the value <code>true</code>.
	 * </ul>
	 *
	 * @param other a bit vector.
	 * @throws IllegalArgumentException if <tt>size() &gt; other.size()</tt>.
	 */
	public void xor(BitVector other) {
		checkSize(other);
		final long[] theBits = this.bits; // cached for speed.
		final long[] otherBits = other.bits; // cached for speed.
		for (int i = theBits.length; --i >= 0;)
			theBits[i] ^= otherBits[i];
	}
}