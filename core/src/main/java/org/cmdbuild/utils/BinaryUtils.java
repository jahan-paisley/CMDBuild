package org.cmdbuild.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class BinaryUtils {

	public static byte[] toByte(final Object obj) throws IOException {
		if (obj instanceof byte[]) {
			return (byte[]) obj;
		}
		byte[] bytes = new byte[0];
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream objOut;

		objOut = new ObjectOutputStream(out);
		objOut.writeObject(obj);
		objOut.flush();
		bytes = out.toByteArray();
		objOut.close();

		return bytes;
	}

	public static Object fromByte(final byte[] bin) throws IOException, ClassNotFoundException {
		Object obj = null;
		ObjectInputStream stream = null;
		if (bin != null) {
			// Deserialize from a byte array
			stream = new ObjectInputStream(new ByteArrayInputStream(bin));
			obj = stream.readObject();
			stream.close();
		}
		return obj;
	}
}
