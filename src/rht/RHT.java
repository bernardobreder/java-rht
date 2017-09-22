package rht;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class RHT {

	private BufferInputStream input;

	private List<String> lines;

	private final List<Runnable> listeners = new ArrayList<Runnable>();

	public RHT(InputStream input) {
		this.lines = new Vector<String>();
		this.input = new BufferInputStream(input);
		new Thread(new Runnable() {

			@Override
			public void run() {
				execute();
			}
		}, "RHT").start();
	}

	private RHT(RHT self) {
		this.lines = self.lines;
	}

	public void addListener(Runnable r) {
		this.listeners.add(r);
	}

	protected void execute() {
		try {
			for (;;) {
				int c = this.input.read();
				if (c == -1) {
					return;
				} else if (c == '#') {
					c = this.input.read();
					if (c == -1) {
						return;
					} else if (c == '#') {
					} else if (c == ' ') {
						this.lines.add("h1." + executeString(0));
						this.notifyChanged();
					} else {

					}
				} else if (c == '\n') {
					this.lines.add("h0.");
				} else {
					this.lines.add("h0." + executeString(c));
					this.notifyChanged();
				}
			}
		} catch (IOException e) {
		} finally {
			this.listeners.clear();
			try {
				this.input.close();
			} catch (IOException e) {
			}
		}
	}

	public RHT clone() {
		return new RHT(this);
	}

	public int getLineCount() {
		return this.lines.size();
	}

	public String getLineContent(int index) {
		return this.lines.get(index).substring(3);
	}

	public String getLineData(int index) {
		return this.lines.get(index);
	}

	public int getLineLevel(int index) {
		return this.lines.get(index).charAt(1) - '0';
	}

	private String executeString(int c) throws IOException {
		if (c == '\n') {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		if (c > 0) {
			sb.append((char) c);
		}
		for (;;) {
			c = this.input.read();
			if (c == '\n' || c == -1) {
				break;
			}
			if (c <= 0x7F) {
			} else if ((c >> 5) == 0x6) {
				int i2 = input.read();
				c = ((c & 0x1F) << 6) + (i2 & 0x3F);
			} else {
				int i2 = input.read();
				int i3 = input.read();
				c = ((c & 0xF) << 12) + ((i2 & 0x3F) << 6) + (i3 & 0x3F);
			}
			sb.append((char) c);
		}
		return sb.toString();
	}

	private void notifyChanged() {
		if (!this.listeners.isEmpty()) {
			for (Runnable r : this.listeners) {
				r.run();
			}
		}
	}

	private static class BufferInputStream extends InputStream {

		private InputStream input;

		private byte[] bytes;

		private int[] array;

		private int index;

		private int max;

		public BufferInputStream(InputStream input) {
			this.input = input;
			this.index = 0;
			this.max = 0;
		}

		@Override
		public int read() throws IOException {
			if (max < 0) {
				return -1;
			}
			if (bytes == null || index == max) {
				if (bytes == null) {
					bytes = new byte[1024];
					array = new int[1024];
				}
				max = this.input.read(bytes);
				for (int n = 0; n < max; n++) {
					byte b = bytes[n];
					array[n] = ((int) b) < 0 ? 2 * Byte.MAX_VALUE + 2
							+ ((int) b) : ((int) b);
				}
				index = 0;
				if (max == -1) {
					return -1;
				}
			}
			return array[index++];
		}

	}

}
