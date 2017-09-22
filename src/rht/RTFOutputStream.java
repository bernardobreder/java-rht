package rht;

import java.io.IOException;
import java.io.OutputStream;

public class RTFOutputStream {

	private OutputStream output;

	public RTFOutputStream(OutputStream output) throws IOException {
		this.output = output;
		this.output.write("{\\rtf1\\ansi\n".getBytes());
	}

	public void close() throws IOException {
		output.write('}');
	}

	public void openBold() throws IOException {
		output.write('\\');
		output.write('b');
		output.write(' ');
	}

	public void closeBold() throws IOException {
		output.write('\\');
		output.write('b');
		output.write('0');
	}

	public void addLine() throws IOException {
		output.write('\\');
		output.write('\n');
	}

	public void addFontSize(int size) throws IOException {
		output.write('\\');
		output.write('f');
		output.write('s');
		output.write(Integer.valueOf(size).toString().getBytes());
	}

	public void addString(String text) throws IOException {
		int size = text.length();
		for (int n = 0; n < size; n++) {
			int c = text.charAt(n);
			if (c <= 0x7F) {
				output.write((char) c);
			} else {
				output.write('\\');
				output.write('\'');
				char[] chars = byte2hex(c);
				output.write(chars[0]);
				output.write(chars[1]);
			}
		}
	}

	public static void main(String[] args) {
		int a = 'á';
		char b = 'á';
		System.out.println(a);
		System.out.println((int) b);
		System.out.println(byte2hex(a));
	}

	private static char[] byte2hex(int b) {
		String alpha = "0123456789abcdef";
		char[] chars = new char[2];
		int c = b % 16;
		int d = b / 16;
		chars[0] = alpha.charAt(d);
		chars[1] = alpha.charAt(c);
		return chars;
	}

}
