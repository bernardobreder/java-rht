package rht;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

public class RHTPanel extends JScrollPane {

	private RHT model;

	private RHTPane pane;

	private static final float PRINT_FONT_PERCENT = 5f / 10;

	private static final float RTF_FONT_PERCENT = 20f / 10;

	public RHTPanel(String text) {
		this(new RHT(new ByteArrayInputStream(text.getBytes(Charset
				.forName("utf-8")))));
	}

	public RHTPanel(RHT model) {
		super();
		this.model = model;
		this.getVerticalScrollBar().setUnitIncrement(10);
//		this.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.getViewport().setView(this.pane = new RHTPane(model));
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				refreshSize();
				repaint();
			}
		});
	}
	
	public void refreshSize(){
		this.pane.refreshSize();
	}

	public void print() throws PrinterException {
		this.pane.print();
	}

	public void exportRtf(OutputStream output) throws IOException {
		RTFOutputStream rtfOutput = new RTFOutputStream(output);
		try {
			int count = this.model.getLineCount();
			for (int n = 0; n < count; n++) {
				String lineContent = this.model.getLineContent(n);
				int lineLevel = this.model.getLineLevel(n);
				Font font;
				switch (lineLevel) {
				case 1: {
					font = this.pane.head1Font;
					break;
				}
				default: {
					font = this.pane.defaultFont;
				}
				}
				int fontSize = (int) (font.getSize() * RTF_FONT_PERCENT);
				boolean isBold = font.isBold();
				rtfOutput.addFontSize(fontSize);
				if (isBold) {
					rtfOutput.openBold();
				}
				rtfOutput.addString(lineContent);
				if (isBold) {
					rtfOutput.closeBold();
				}
				rtfOutput.addLine();
			}
		} finally {
			rtfOutput.close();
		}
	}

	public void setDefaultFont(Font font) {
		this.pane.defaultFont = font;
	}

	public void setHead1Font(Font font) {
		this.pane.head1Font = font;
	}

	public void setHead2Font(Font font) {
		this.pane.head2Font = font;
	}

	public void setHead3Font(Font font) {
		this.pane.head3Font = font;
	}

	public void setHead4Font(Font font) {
		this.pane.head4Font = font;
	}

	public void setHead5Font(Font font) {
		this.pane.head5Font = font;
	}

	public class RHTPane extends JComponent {

		private RHT model;

		private Font defaultFont;

		private Font head1Font;

		private Font head2Font;

		private Font head3Font;

		private Font head4Font;

		private Font head5Font;

		private final List<LineStyle> lines = new ArrayList<LineStyle>();

		public RHTPane(RHT model) {
			this.model = model;
			this.model.addListener(new Runnable() {
				@Override
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							refreshSize();
							repaint();
						}
					});
				}
			});
			this.defaultFont = new Font("Lucida Console", Font.PLAIN, 12);
			this.head1Font = new Font("Monospaced", Font.BOLD, 24);
			this.head2Font = head1Font.deriveFont(20f);
			this.head3Font = head1Font.deriveFont(18f);
			this.head4Font = head1Font.deriveFont(16f);
			this.head5Font = head1Font.deriveFont(14f);
		}

		/**
		 * @throws PrinterException
		 */
		private void print() throws PrinterException {
			PrinterJob printJob = PrinterJob.getPrinterJob();
			final RHTPanel panel = new RHTPanel(model.clone());
			final PageFormat defaultPageFormat = printJob.defaultPage();
			final int pageX = 50;
			final int pageY = 50;
			final int pageWidth = (int) (defaultPageFormat.getWidth() - 2 * pageX);
			final int pageHeight = (int) (defaultPageFormat.getHeight() - 2 * pageY);
			printJob.setPrintable(new Printable() {
				@Override
				public int print(Graphics g, PageFormat pageFormat,
						int pageIndex) throws PrinterException {
					int height = pageHeight;
					int currentPageIndex = 0;
					int beginLine = 0;
					List<LineStyle> lines = panel.pane.lines;
					if (currentPageIndex != pageIndex) {
						for (beginLine = 0; beginLine < lines.size(); beginLine++) {
							LineStyle lineStyle = lines.get(beginLine);
							if (height - lineStyle.height < 0) {
								if (++currentPageIndex == pageIndex) {
									break;
								}
								height = pageHeight;
							}
							height -= lineStyle.height;
						}
					}
					height = pageHeight;
					int endLine;
					for (endLine = beginLine; endLine < lines.size(); endLine++) {
						LineStyle lineStyle = lines.get(endLine);
						if (height - lineStyle.height < 0) {
							break;
						}
						height -= lineStyle.height;
					}
					{
						int x = pageX;
						int y = pageY;
						for (int n = beginLine; n < endLine; n++) {
							LineStyle lineStyle = lines.get(n);
							g.setFont(lineStyle.font);
							g.setColor(lineStyle.color);
							if (g.getFontMetrics().stringWidth(
									lineStyle.content) > pageWidth) {
								g.getFontMetrics()
										.stringWidth(
												"a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l ");
								System.out.println();
							}
							g.drawString(lineStyle.content, x, y
									+ lineStyle.height);
							y += lineStyle.height;
						}
					}
					return beginLine != endLine ? PAGE_EXISTS : NO_SUCH_PAGE;
				}
			});
			if (printJob.printDialog()) {
				panel.setSize(pageWidth, pageHeight);
				panel.pane.refreshSize();
				printJob.print();
			}
		}

		public int getWidthSpace() {
			int width = RHTPanel.this.getWidth();
			if (RHTPanel.this.getVerticalScrollBar().isVisible()) {
				width -= RHTPanel.this.getVerticalScrollBar().getWidth();
			}
			return width;
		}

		public void refreshSize() {
			System.out.println("Refresh");
			this.lines.clear();
			int scrollWidth = this.getWidthSpace();
			int paneHeight = 5;
			StringBuilder sb = new StringBuilder();
			int size = this.model.getLineCount();
			for (int n = 0; n < size; n++) {
				String content = this.model.getLineContent(n);
				int level = this.model.getLineLevel(n);
				Font font;
				Color color;
				switch (level) {
				case 1: {
					font = this.head1Font;
					color = Color.GRAY;
					break;
				}
				default: {
					font = this.defaultFont;
					color = Color.BLACK;
				}
				}
				FontMetrics fontMetrics = this.getFontMetrics(font);
				int lineHeight = fontMetrics.getHeight();
				int spaceWidth = fontMetrics.charWidth(' ');
				StringTokenizer tokenizer = new StringTokenizer(content);
				int x = 0;
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					int stringWidth = fontMetrics.stringWidth(token);
					if (x + stringWidth > scrollWidth) {
						paneHeight += lineHeight;
						fontMetrics.stringWidth(sb.toString());
						this.lines.add(new LineStyle(sb.toString(), font,
								color, lineHeight));
						sb.delete(0, sb.length());
						x = stringWidth + spaceWidth;
						sb.append(token);
						sb.append(' ');
					} else {
						x += stringWidth + spaceWidth;
						sb.append(token);
						sb.append(' ');
					}
				}
				this.lines.add(new LineStyle(sb.toString(), font, color,
						lineHeight));
				sb.delete(0, sb.length());
				paneHeight += lineHeight;
			}
			this.setPreferredSize(new Dimension(scrollWidth, paneHeight));
		}

		@Override
		protected void paintComponent(Graphics g) {
			this.paint(g, RHTPanel.this.getViewport().getViewRect());
		}

		public void paint(Graphics g, Rectangle rect) {
			int width = rect.width;
			Font font = g.getFont();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			boolean foundTransY = false;
			Rectangle otherRect = new Rectangle();
			int y = 0;
			for (int n = 0; n < this.lines.size(); n++) {
				LineStyle lineStyle = this.lines.get(n);
				otherRect.setRect(0, y, width, lineStyle.height);
				if (otherRect.intersects(rect)) {
					if (!foundTransY) {
						foundTransY = true;
					}
					g.setFont(lineStyle.font);
					g.setColor(lineStyle.color);
					g.drawString(lineStyle.content, 0, y + lineStyle.height);
				} else if (foundTransY) {
					break;
				}
				y += lineStyle.height;
			}
			g.setFont(font);
		}

	}

	private static class LineStyle {

		private String content;
		private Font font;
		private Color color;
		private int height;

		public LineStyle(String content, Font font, Color color, int textHeight) {
			super();
			this.content = content;
			this.font = font;
			this.color = color;
			this.height = textHeight;
		}

	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				String code = "# Intro\n\na b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z a b c d e f g h i j k l m n o p q r s t u v x y z Era um teste que \n\nss\nirei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n# Intro\n\nEra um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que um teste que irei fazer isso é para que\nVez um\nTexto\n# Indice interno\n\nação\n";
				// String code =
				// "a\nb\nc\nd\ne\nf\ng\nh\ni\nj\nk\nl\nm\nn\no\np\nq\nr\ns\nt\nu\nx\ny\nz\na\nb\nc\nd\ne\nf\ng\nh\ni\nj\nk\nl\nm\nn\no\np\nq\nr\ns\nt\nu\nx\ny\nz\na\nb\nc\nd\ne\nf\ng\nh\ni\nj\nk\nl\nm\nn\no\np\nq\nr\ns\nt\nu\nx\ny\nz\na\nb\nc\nd\ne\nf\ng\nh\ni\nj\nk\nl\nm\nn\no\np\nq\nr\ns\nt\nu\nx\ny\nz\n";
				final RHTPanel panel = new RHTPanel(new RHT(
						new ByteArrayInputStream(code.getBytes(Charset
								.forName("utf-8")))));
				panel.setPreferredSize(new Dimension(495, 741));
				JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5,
						5));
				{
					JButton print = new JButton("Imprimir...");
					print.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								panel.print();
							} catch (PrinterException e1) {
								e1.printStackTrace();
								JOptionPane.showMessageDialog(frame,
										e1.getClass().getSimpleName() + ": "
												+ e1.getMessage());
							}
						}
					});
					buttons.add(print);
				}
				{
					JButton print = new JButton("Exportar...");
					print.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								FileOutputStream output = new FileOutputStream(
										"a.rtf");
								try {
									panel.exportRtf(output);
								} catch (IOException e1) {
									e1.printStackTrace();
									JOptionPane.showMessageDialog(frame, e1
											.getClass().getSimpleName()
											+ ": "
											+ e1.getMessage());
								} finally {
									try {
										output.close();
									} catch (IOException e1) {
									}
								}
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					});
					buttons.add(print);
				}
				frame.add(buttons, BorderLayout.SOUTH);
				frame.add(panel);
				frame.pack();
				// frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
				frame.setVisible(true);
			}
		});
	}

}
