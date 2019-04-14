import java.awt.image.*;
import java.util.*;
import java.awt.*;
import java.io.*;
import javax.imageio.*;

public class Visualizer {
	public static final int WIDTH = 1920, HEIGHT = 1080, TICK_HEIGHT = 5;
	private static final double XBOUND = 3, YMIN = -1.1, YMAX = 2.1;
	private static double offset = 0;
	private static int MORPH_FRAMES = 30;
	private static final boolean MORPH = true;
	private static ArrayList<Double> xCoeffs = new ArrayList<>();
	private static ArrayList<Double> sinCoeffs = new ArrayList<>();
	private static ArrayList<Double> cosCoeffs = new ArrayList<>();
	private static double xCoord(double x) {
		return (x-XBOUND) / (2*XBOUND) * WIDTH + WIDTH;
	}
	private static double yCoord(double y) {
		return (YMIN-y) / (YMAX-YMIN) * HEIGHT + HEIGHT;
	}
	private static BufferedImage createBackground() {
		BufferedImage bi = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		Color gray = new Color(200, 200, 200);
		for(int i = -(int)Math.ceil(XBOUND); i <= (int)Math.floor(XBOUND); i++) {
			g.setColor(gray);
			g.drawLine((int)xCoord(i), 0, (int)xCoord(i), HEIGHT);
			g.setColor(Color.BLACK);
			g.drawLine((int)xCoord(i), -TICK_HEIGHT+(int)yCoord(0), (int)xCoord(i), TICK_HEIGHT+(int)yCoord(0));
		}
		for(int i = (int)Math.ceil(YMIN); i <= (int)Math.floor(YMAX); i++) {
			g.setColor(gray);
			g.drawLine(0, (int)yCoord(i), WIDTH, (int)yCoord(i));
			g.setColor(Color.BLACK);
			g.drawLine(-TICK_HEIGHT+WIDTH/2, (int)yCoord(i), TICK_HEIGHT+WIDTH/2, (int)yCoord(i));
		}
		g.setColor(Color.BLACK);
		g.drawLine(0, (int)yCoord(0), WIDTH, (int)yCoord(0));
		g.drawLine(WIDTH/2, 0, WIDTH/2, HEIGHT);

		//DRAW ORIGINAL FUNCTION
		g.setColor(Color.BLUE);
		for(int i = 0; i < WIDTH; i++) {
			double x = ((double)i)/WIDTH * XBOUND * 2 - XBOUND;
			double nx = ((double)i+1)/WIDTH * XBOUND * 2 - XBOUND;
			g.drawLine(i, x > 0 ? (int)yCoord(1) : (int)yCoord(0), i+1, nx > 0 ? (int)yCoord(1) : (int)yCoord(0));
		}

		return bi;
	}
	public static void main(String[] args) throws IOException {
		Scanner scan = new Scanner(new File("input.txt"));
		offset = scan.nextDouble();
		for(int i = 0; scan.hasNext(); i++) {
			scan.skip("\\s*\\+\\s*");
			String[] cos_parts = scan.next().replaceAll("[\\(\\)\\s\\*x]", "").split("cos");
			if(cos_parts.length != 2) throw new RuntimeException("err parsing cos");
			xCoeffs.add(Double.parseDouble(cos_parts[1]));
			cosCoeffs.add(Double.parseDouble(cos_parts[0]));
			scan.skip("\\s*\\+\\s*");
			String[] sin_parts = scan.next().replaceAll("[\\(\\)\\s\\*x]", "").split("sin");
			if(sin_parts.length != 2) throw new RuntimeException("err parsing sin");
			if(xCoeffs.get(i) != Double.parseDouble(sin_parts[1])) throw new RuntimeException("sin/cos mismatch");
			sinCoeffs.add(Double.parseDouble(sin_parts[0]));
		}
		BufferedImage background = createBackground();
		int sin_num = 0;
		int cos_num = 0;
		double[] vals = new double[WIDTH+1];
		for(int i = 0; i < WIDTH+1; i++) {
			vals[i] = offset;
		}
		double[] vals_old = new double[0], vals_diff = new double[0];
		if(MORPH) {
			vals_old = Arrays.copyOf(vals, vals.length);
			vals_diff = new double[vals.length];
		}
		int nth_image = 0;
		int morph_tick = 0;
		BufferedImage bim = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bim.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		while(true) {
			int mf = 0;
			if(MORPH) {
				for(int i = 0; i < WIDTH+1; i++) {
					vals_diff[i] = vals[i] - vals_old[i];
				}
				if(MORPH_FRAMES > 6) {
					MORPH_FRAMES--;
				} else if(MORPH_FRAMES > 0) {
					if(morph_tick++ >= 12 - 2 * MORPH_FRAMES) {
						MORPH_FRAMES--;
						morph_tick = 0;
					}
				}
			}
			do {
				g.setColor(Color.RED);
				g.drawImage(background, 0, 0, null);
				for(int i = 0; i < WIDTH; i++) {
					g.drawLine(i, (int)yCoord(MORPH ? vals_old[i] : vals[i]), i+1, (int)yCoord(MORPH ? vals_old[i+1] : vals[i+1]));
				}
				g.setColor(Color.BLACK);
				g.drawString("cos terms: " + cos_num + ", sin terms: " + sin_num + (MORPH ? ", morph: " + mf : ""), 10, 25);
				ImageIO.write(bim, "png", new File("out/" + nth_image + ".png"));
				System.out.println("wrote: " + nth_image + ".png");
				++mf;
				++nth_image;
				if(MORPH) {
					for(int i = 0; i < WIDTH+1; i++) {
						vals_old[i] += vals_diff[i] / MORPH_FRAMES;
					}
				}
			} while(mf < MORPH_FRAMES);
			if(MORPH) {
				for(int i = 0; i < WIDTH + 1; i++) {
					vals_old[i] = vals[i];
				}
			}
			/*if(cos_num <= sin_num && cos_num < cosCoeffs.size()) {
				for(int i = 0; i < WIDTH+1; i++) {
					double x = ((double)i)/WIDTH * XBOUND * 2 - XBOUND;
					vals[i] += cosCoeffs.get(cos_num) * Math.cos(x * xCoeffs.get(cos_num));
				}
				cos_num++;
			} else {*/
				if(sin_num < sinCoeffs.size()) {
					for(int i = 0; i < WIDTH+1; i++) {
						double x = ((double)i)/WIDTH * XBOUND * 2 - XBOUND;
						vals[i] += sinCoeffs.get(sin_num) * Math.sin(x * xCoeffs.get(sin_num));
					}
					//sin_term++;

	
					//DEL:ETE ME DELETE ME DELETE ME
					sin_num+=2;
					cos_num = sin_num; //TODO DELETE ME
					//END DELETE ME DELETE ME DELETE ME
				} else {
					break;
				}
			//}
		}
	}
}
