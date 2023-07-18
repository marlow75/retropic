package pl.dido.image.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {
	
	public static InputStream getResourceAsStream(final String fileName, final Class<?> clazz) {
		final ClassLoader classLoader = clazz.getClassLoader();
		return classLoader.getResourceAsStream(fileName);
	}

	public static InputStream getResourceAsStream(final String fileName) {
		final ClassLoader classLoader = Utils.class.getClassLoader();
		return classLoader.getResourceAsStream(fileName);
	}

	public static URL getResourceAsURL(final String fileName) {
		final ClassLoader classLoader = Utils.class.getClassLoader();
		return classLoader.getResource(fileName);
	}

	public static String createDirectory(final String directory) throws IOException {
		final Path path = Paths.get(directory);
		return (!Files.isDirectory(path)) ? Files.createDirectory(path).toString() : path.toString();
	}

	public static byte[] loadCharset(final InputStream is) throws IOException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		final byte[] data = new byte[8];

		while ((nRead = is.read(data, 0, data.length)) != -1)
			buffer.write(data, 0, nRead);

		buffer.flush();
		return buffer.toByteArray();
	}
}
