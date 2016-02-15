package mcib_plugins;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import ij.*;
import ij.plugin.*;
import ij.plugin.frame.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.frame.Recorder;
import ij.plugin.filter.Analyzer;

/**
 *  Adjusts the lower and upper threshold levels of the active image. This class
 *  is multi-threaded to provide a more responsive user interface.
 *
 * @author     modified by thomas for hysteresis
 * @created    5 janvier 2007
 */
public class ConnectionThresholding_ extends PlugInFrame implements PlugIn, Measurements,
		Runnable, ActionListener, AdjustmentListener, ItemListener {

	final static int RED = 0, BLACK_AND_WHITE = 1, OVER_UNDER = 2;
	final static String[] modes = {"Red", "Black & White", "Over/Under"};
	final static double defaultMinThreshold = 85;
	final static double defaultMaxThreshold = 170;
	static boolean fill1 = true;
	static boolean fill2 = true;
	static boolean useBW = true;
	static boolean backgroundToNaN = true;
	static Frame instance;
	static int mode = RED;
	ThresholdPlot plot = new ThresholdPlot();
	Thread thread;

	int minValue = -1;
	int maxValue = -1;
	int sliderRange = 256;
	boolean doAutoAdjust, doReset, doApplyLut, doStateChange, doSet;

	Panel panel;
	Button autoB, resetB, applyB, setB;
	int previousImageID;
	int previousImageType;
	double previousMin, previousMax;
	ImageJ ij;
	double minThreshold, maxThreshold;
	int minusHyst = 5;
	int plusHyst = 5;
	int minusHystValue, plusHystValue;
	// 0-255
	Scrollbar minSlider, maxSlider;
	Scrollbar minhystSlider, maxhystSlider;
	Label label1, label2;
	Label label3, label4;
	boolean done;
	boolean invertedLut;
	int lutColor;
	static Choice choice;
	boolean firstActivation;

	// Only work on same image
	ImagePlus impOrig;
	ImageProcessor ipOrig;


	/**
	 *  Constructor for the ThresholdAdjuster object
	 */
	public ConnectionThresholding_() {
		super("Connection Thresholding");
		if (instance != null) {
			instance.toFront();
			return;
		}

		WindowManager.addWindow(this);
		instance = this;
		//setLutColor(mode);
		IJ.register(PasteController.class);

		ij = IJ.getInstance();
		Font font = new Font("SansSerif", Font.PLAIN, 10);
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);

		// plot
		int y = 0;
		c.gridx = 0;
		c.gridy = y++;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(10, 10, 0, 10);
		add(plot, c);
		plot.addKeyListener(ij);

		// minThreshold slider
		minSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange / 3, 1, 0, sliderRange);
		c.gridx = 0;
		c.gridy = y++;
		c.gridwidth = 1;
		c.weightx = IJ.isMacintosh() ? 90 : 100;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 10, 0, 0);
		add(minSlider, c);
		minSlider.addAdjustmentListener(this);
		minSlider.addKeyListener(ij);
		minSlider.setUnitIncrement(1);

		// minThreshold slider label
		c.gridx = 1;
		c.gridwidth = 1;
		c.weightx = IJ.isMacintosh() ? 10 : 0;
		c.insets = new Insets(5, 0, 0, 10);
		label1 = new Label("      ", Label.RIGHT);
		label1.setFont(font);
		add(label1, c);

		// maxThreshold slider
		maxSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange * 2 / 3, 1, 0, sliderRange);
		c.gridx = 0;
		c.gridy = y++;
		c.gridwidth = 1;
		c.weightx = 100;
		c.insets = new Insets(0, 10, 0, 0);
		add(maxSlider, c);
		maxSlider.addAdjustmentListener(this);
		maxSlider.addKeyListener(ij);
		maxSlider.setUnitIncrement(1);

		// maxThreshold slider label
		c.gridx = 1;
		c.gridwidth = 1;
		c.weightx = 0;
		c.insets = new Insets(0, 0, 0, 10);
		label2 = new Label("      ", Label.RIGHT);
		label2.setFont(font);
		add(label2, c);

		// minhyst slider
		minhystSlider = new Scrollbar(Scrollbar.HORIZONTAL, 5, 1, 0, sliderRange);
		c.gridx = 0;
		c.gridy = y++;
		c.gridwidth = 1;
		c.weightx = IJ.isMacintosh() ? 90 : 100;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 10, 0, 0);
		add(minhystSlider, c);
		minhystSlider.addAdjustmentListener(this);
		minhystSlider.addKeyListener(ij);
		minhystSlider.setUnitIncrement(1);

		// minhyst slider label
		c.gridx = 1;
		c.gridwidth = 1;
		c.weightx = IJ.isMacintosh() ? 10 : 0;
		c.insets = new Insets(0, 0, 0, 10);
		label3 = new Label("      ", Label.RIGHT);
		label3.setFont(font);
		add(label3, c);

		// maxhyst slider
		maxhystSlider = new Scrollbar(Scrollbar.HORIZONTAL, 5, 1, 0, sliderRange);
		c.gridx = 0;
		c.gridy = y++;
		c.gridwidth = 1;
		c.weightx = IJ.isMacintosh() ? 90 : 100;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 10, 0, 0);
		add(maxhystSlider, c);
		maxhystSlider.addAdjustmentListener(this);
		maxhystSlider.addKeyListener(ij);
		maxhystSlider.setUnitIncrement(1);

		// maxhyst slider label
		c.gridx = 1;
		c.gridwidth = 1;
		c.weightx = IJ.isMacintosh() ? 10 : 0;
		c.insets = new Insets(0, 0, 0, 10);
		label4 = new Label("      ", Label.RIGHT);
		label4.setFont(font);
		add(label4, c);

		// choice
		/*
		 *  choice = new Choice();
		 *  for (int i = 0; i < modes.length; i++) {
		 *  choice.addItem(modes[i]);
		 *  }
		 *  choice.select(mode);
		 *  choice.addItemListener(this);
		 *  choice.addKeyListener(ij);
		 */
		//c.gridx = 0;
		//c.gridy = y++;
		//c.gridwidth = 2;
		//c.insets = new Insets(5, 5, 0, 5);
		//c.anchor = GridBagConstraints.CENTER;
		//c.fill = GridBagConstraints.NONE;
		//add(choice, c);

		// buttons
		int trim = IJ.isMacOSX() ? 11 : 0;
		panel = new Panel();
		autoB = new TrimmedButton("Auto", trim);
		autoB.addActionListener(this);
		autoB.addKeyListener(ij);
		panel.add(autoB);
		applyB = new TrimmedButton("Multi", trim);
		applyB.addActionListener(this);
		applyB.addKeyListener(ij);
		panel.add(applyB);
		resetB = new TrimmedButton("Hyst", trim);
		resetB.addActionListener(this);
		resetB.addKeyListener(ij);
		panel.add(resetB);
		setB = new TrimmedButton("Set", trim);
		setB.addActionListener(this);
		setB.addKeyListener(ij);
		panel.add(setB);
		c.gridx = 0;
		c.gridy = y++;
		c.gridwidth = 2;
		c.insets = new Insets(0, 5, 10, 5);
		add(panel, c);

		addKeyListener(ij);
		// ImageJ handles keyboard shortcuts
		pack();
		GUI.center(this);
		firstActivation = true;
		if (IJ.isMacOSX()) {
			setResizable(false);
		}
		show();

		thread = new Thread(this, "Hysteresis_");
		//thread.setPriority(thread.getPriority()-1);
		thread.start();
		impOrig = WindowManager.getCurrentImage();
		if (impOrig != null) {
			ipOrig = setup(impOrig);
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
		if (e.getSource() == minSlider) {
			minValue = minSlider.getValue();
		} else if (e.getSource() == maxSlider) {
			maxValue = maxSlider.getValue();
		} else if (e.getSource() == minhystSlider) {
			minusHystValue = minhystSlider.getValue();
		} else {
			plusHystValue = maxhystSlider.getValue();
		}
		notify();
	}


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public synchronized void actionPerformed(ActionEvent e) {
		Button b = (Button) e.getSource();
		if (b == null) {
			return;
		}
		if (b == resetB) {
			doReset = true;
		} else if (b == autoB) {
			doAutoAdjust = true;
		} else if (b == applyB) {
			doApplyLut = true;
		} else if (b == setB) {
			doSet = true;
		}
		notify();
	}



	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public synchronized void itemStateChanged(ItemEvent e) {
		mode = choice.getSelectedIndex();
		//setLutColor(mode);
		doStateChange = true;
		notify();
	}


	/**
	 *  Description of the Method
	 *
	 * @param  imp  Description of the Parameter
	 * @return      Description of the Return Value
	 */
	ImageProcessor setup(ImagePlus imp) {
		ImageProcessor ip;
		int type = imp.getType();
		if (type == ImagePlus.COLOR_RGB) {
			return null;
		}
		ip = imp.getProcessor();
		boolean minMaxChange = false;
		boolean not8Bits = type == ImagePlus.GRAY16 || type == ImagePlus.GRAY32;
		if (not8Bits) {
			if (ip.getMin() == plot.stackMin && ip.getMax() == plot.stackMax) {
				minMaxChange = false;
			} else if (ip.getMin() != previousMin || ip.getMax() != previousMax) {
				minMaxChange = true;
				previousMin = ip.getMin();
				previousMax = ip.getMax();
			}
		}
		int id = imp.getID();
		if (minMaxChange || id != previousImageID || type != previousImageType) {
			//IJ.log(minMaxChange +"  "+ (id!=previousImageID)+"  "+(type!=previousImageType));
			if (not8Bits && minMaxChange) {
				ip.resetMinAndMax();
				imp.updateAndDraw();
			}
			invertedLut = imp.isInvertedLut();
			minThreshold = ip.getMinThreshold();
			maxThreshold = ip.getMaxThreshold();
			ImageStatistics stats = plot.setHistogram(imp, false);
			if (minThreshold == ip.NO_THRESHOLD) {
				autoSetLevels(ip, stats);
			} else {
				minThreshold = scaleDown(ip, minThreshold);
				maxThreshold = scaleDown(ip, maxThreshold);
			}
			scaleUpAndSet(ip, minThreshold, maxThreshold, minusHyst, plusHyst);
			updateLabels(imp, ip);
			updatePlot();
			updateScrollBars();
			imp.updateAndDraw();
		}
		previousImageID = id;
		previousImageType = type;
		return ip;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  ip     Description of the Parameter
	 * @param  stats  Description of the Parameter
	 */
	void autoSetLevels(ImageProcessor ip, ImageStatistics stats) {
		if (stats == null || stats.histogram == null) {
			minThreshold = defaultMinThreshold;
			maxThreshold = defaultMaxThreshold;
			return;
		}
		int threshold = ip.getAutoThreshold(stats.histogram);
		//IJ.log(threshold+"  "+stats.min+"  "+stats.max+"  "+stats.dmode);
		int count1 = 0;
		//IJ.log(threshold+"  "+stats.min+"  "+stats.max+"  "+stats.dmode);
		int count2 = 0;
		for (int i = 0; i < 256; i++) {
			if (i < threshold) {
				count1 += stats.histogram[i];
			} else {
				count2 += stats.histogram[i];
			}
		}
		boolean unbalanced = (double) count1 / count2 > 1.25 || (double) count2 / count1 > 1.25;
		//IJ.log(unbalanced+"  "+count1+"  "+count2);
		double lower;
		//IJ.log(unbalanced+"  "+count1+"  "+count2);
		double upper;
		if (unbalanced) {
			if ((stats.max - stats.dmode) > (stats.dmode - stats.min)) {
				minThreshold = threshold;
				maxThreshold = stats.max;
			} else {
				minThreshold = stats.min;
				maxThreshold = threshold;
			}
		} else {
			if (ip.isInvertedLut()) {
				minThreshold = threshold;
				maxThreshold = 255;
			} else {
				minThreshold = 0;
				maxThreshold = threshold;
			}
		}
		if (Recorder.record) {
			Recorder.record("setAutoThreshold");
		}
	}


	/**
	 *  Scales threshold levels in the range 0-255 to the actual levels.
	 *
	 * @param  ip            Description of the Parameter
	 * @param  minThreshold  Description of the Parameter
	 * @param  maxThreshold  Description of the Parameter
	 * @param  minus         Description of the Parameter
	 * @param  plus          Description of the Parameter
	 */
	void scaleUpAndSet(ImageProcessor ip, double minThreshold, double maxThreshold, int minus, int plus) {

		double min = scaleUp(ip, minThreshold);
		double max = scaleUp(ip, maxThreshold);

		//ip.setThreshold(minThreshold, maxThreshold, lutColor);
		byte[] rLUT2 = new byte[256];
		byte[] gLUT2 = new byte[256];
		byte[] bLUT2 = new byte[256];
		double ii;
		for (int i = 0; i < 256; i++) {
			// thresholded
			ii = scaleUp(ip, i);
			if (ii >= min && ii <= max) {
				rLUT2[i] = (byte) 255;
				gLUT2[i] = (byte) 0;
				bLUT2[i] = (byte) 0;
			}
			// lower thresholded
			else if ((ii >= min - minus) && (ii < min)) {
				rLUT2[i] = (byte) 0;
				gLUT2[i] = (byte) 0;
				bLUT2[i] = (byte) 255;
			}
			// upper thresholded
			else if ((ii > max) && (ii <= max + plus)) {
				rLUT2[i] = (byte) 0;
				gLUT2[i] = (byte) 255;
				bLUT2[i] = (byte) 0;
			}
			// not thresholded
			else {
				rLUT2[i] = (byte) 0;
				gLUT2[i] = (byte) 0;
				bLUT2[i] = (byte) 0;
			}
		}

		ColorModel cm = new IndexColorModel(8, 256, rLUT2, gLUT2, bLUT2);
		ip.setColorModel(cm);
	}


	/**
	 *  Scales a threshold level to the range 0-255.
	 *
	 * @param  ip         Description of the Parameter
	 * @param  threshold  Description of the Parameter
	 * @return            Description of the Return Value
	 */
	double scaleDown(ImageProcessor ip, double threshold) {
		if (ip instanceof ByteProcessor) {
			return threshold;
		}
		double min = ip.getMin();
		double max = ip.getMax();
		if (max > min) {
			return ((threshold - min) / (max - min)) * 255.0;
		} else {
			return ImageProcessor.NO_THRESHOLD;
		}
	}


	/**
	 *  Scales a threshold level in the range 0-255 to the actual level.
	 *
	 * @param  ip         Description of the Parameter
	 * @param  threshold  Description of the Parameter
	 * @return            Description of the Return Value
	 */
	double scaleUp(ImageProcessor ip, double threshold) {
		double min = ip.getMin();
		double max = ip.getMax();
		if (max > min) {
			return min + (threshold / 255.0) * (max - min);
		} else {
			return ImageProcessor.NO_THRESHOLD;
		}
	}


	/**
	 *  Description of the Method
	 */
	void updatePlot() {
		plot.minThreshold = minThreshold;
		plot.maxThreshold = maxThreshold;
		plot.mode = mode;
		plot.repaint();
	}


	/**
	 *  Description of the Method
	 *
	 * @param  imp  Description of the Parameter
	 * @param  ip   Description of the Parameter
	 */
	void updateLabels(ImagePlus imp, ImageProcessor ip) {
		double min = scaleUp(imp.getProcessor(), minThreshold);
		double max = scaleUp(imp.getProcessor(), maxThreshold);

		label1.setText("" + IJ.d2s(min, 2));
		label2.setText("" + IJ.d2s(max, 2));
		label3.setText("" + minusHyst);
		label4.setText("" + plusHyst);
	}


	/**
	 *  Description of the Method
	 */
	void updateScrollBars() {
		minSlider.setValue((int) minThreshold);
		maxSlider.setValue((int) maxThreshold);
		minhystSlider.setValue(minusHyst);
		maxhystSlider.setValue(plusHyst);
	}


	/**
	 *  Restore image outside non-rectangular roi.
	 *
	 * @param  imp  Description of the Parameter
	 * @param  ip   Description of the Parameter
	 */
	void doMasking(ImagePlus imp, ImageProcessor ip) {
		ImageProcessor mask = imp.getMask();
		if (mask != null) {
			ip.reset(mask);
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  imp    Description of the Parameter
	 * @param  ip     Description of the Parameter
	 * @param  value  Description of the Parameter
	 */
	void adjustMinThreshold(ImagePlus imp, ImageProcessor ip, double value) {
		if (IJ.altKeyDown() || IJ.shiftKeyDown()) {
			double width = maxThreshold - minThreshold;
			if (width < 1.0) {
				width = 1.0;
			}
			minThreshold = value;
			maxThreshold = minThreshold + width;
			if ((minThreshold + width) > 255) {
				minThreshold = 255 - width;
				maxThreshold = minThreshold + width;
				minSlider.setValue((int) minThreshold);
			}
			maxSlider.setValue((int) maxThreshold);
			scaleUpAndSet(ip, minThreshold, maxThreshold, minusHyst, plusHyst);
			return;
		}
		minThreshold = value;
		if (maxThreshold < minThreshold) {
			maxThreshold = minThreshold;
			maxSlider.setValue((int) maxThreshold);
		}
		double min = ip.getMin();
		double mint = scaleUp(ip, minThreshold);
		if (mint - minusHyst < min) {
			minusHyst = (int) (mint - min);
			minhystSlider.setValue(minusHyst);
		}
		scaleUpAndSet(ip, minThreshold, maxThreshold, minusHyst, plusHyst);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  imp    Description of the Parameter
	 * @param  ip     Description of the Parameter
	 * @param  value  Description of the Parameter
	 */
	void adjustMinusHyst(ImagePlus imp, ImageProcessor ip, double value) {
		minusHyst = (int) value;
		double min = ip.getMin();
		double mint = scaleUp(ip, minThreshold);
		if (mint - minusHyst < min) {
			minusHyst = (int) (mint - min);
			minhystSlider.setValue(minusHyst);
		}
		//minusHystValue = 1;
		scaleUpAndSet(ip, minThreshold, maxThreshold, minusHyst, plusHyst);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  imp    Description of the Parameter
	 * @param  ip     Description of the Parameter
	 * @param  value  Description of the Parameter
	 */
	void adjustPlusHyst(ImagePlus imp, ImageProcessor ip, double value) {

		plusHyst = (int) value;
		double max = ip.getMax();
		double maxt = scaleUp(ip, maxThreshold);
		if (maxt + plusHyst > max) {
			plusHyst = (int) (max - maxt);
			maxhystSlider.setValue(plusHyst);
		}
		//plusHystValue = 1;
		scaleUpAndSet(ip, minThreshold, maxThreshold, minusHyst, plusHyst);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  imp     Description of the Parameter
	 * @param  ip      Description of the Parameter
	 * @param  cvalue  Description of the Parameter
	 */
	void adjustMaxThreshold(ImagePlus imp, ImageProcessor ip, int cvalue) {
		maxThreshold = cvalue;
		if (minThreshold > maxThreshold) {
			minThreshold = maxThreshold;
			minSlider.setValue((int) minThreshold);
		}
		double max = ip.getMax();
		double maxt = scaleUp(ip, maxThreshold);
		if (maxt + plusHyst > max) {
			plusHyst = (int) (max - maxt);
			maxhystSlider.setValue(plusHyst);
		}
		scaleUpAndSet(ip, minThreshold, maxThreshold, minusHyst, plusHyst);
		IJ.setKeyUp(KeyEvent.VK_ALT);
		IJ.setKeyUp(KeyEvent.VK_SHIFT);
	}



	/**
	 *  Description of the Method
	 *
	 * @param  imp  Description of the Parameter
	 * @param  ip   Description of the Parameter
	 */
	void doSet(ImagePlus imp, ImageProcessor ip) {
		double level1 = ip.getMinThreshold();
		double level2 = ip.getMaxThreshold();
		if (level1 == ImageProcessor.NO_THRESHOLD) {
			level1 = scaleUp(ip, defaultMinThreshold);
			level2 = scaleUp(ip, defaultMaxThreshold);
		}
		Calibration cal = imp.getCalibration();
		int digits = (ip instanceof FloatProcessor) || cal.calibrated() ? 2 : 0;
		level1 = cal.getCValue(level1);
		level2 = cal.getCValue(level2);
		GenericDialog gd = new GenericDialog("Set Threshold Levels");
		gd.addNumericField("Lower Threshold Level: ", level1, digits);
		gd.addNumericField("Upper Threshold Level: ", level2, digits);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}
		level1 = gd.getNextNumber();
		level2 = gd.getNextNumber();
		level1 = cal.getRawValue(level1);
		level2 = cal.getRawValue(level2);
		if (level2 < level1) {
			level2 = level1;
		}
		double minDisplay = ip.getMin();
		double maxDisplay = ip.getMax();
		ip.resetMinAndMax();
		double minValue = ip.getMin();
		double maxValue = ip.getMax();
		if (level1 < minValue) {
			level1 = minValue;
		}
		if (level2 > maxValue) {
			level2 = maxValue;
		}
		boolean outOfRange = level1 < minDisplay || level2 > maxDisplay;
		if (outOfRange) {
			plot.setHistogram(imp, false);
		} else {
			ip.setMinAndMax(minDisplay, maxDisplay);
		}

		minThreshold = scaleDown(ip, level1);
		maxThreshold = scaleDown(ip, level2);
		scaleUpAndSet(ip, minThreshold, maxThreshold, minusHyst, plusHyst);
		updateScrollBars();
		if (Recorder.record) {
			if (imp.getBitDepth() == 32) {
				Recorder.record("setThreshold", ip.getMinThreshold(), ip.getMaxThreshold());
			} else {
				int min = (int) ip.getMinThreshold();
				int max = (int) ip.getMaxThreshold();
				if (cal.isSigned16Bit()) {
					min = (int) cal.getCValue(level1);
					max = (int) cal.getCValue(level2);
				}
				Recorder.record("setThreshold", min, max);
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  imp  Description of the Parameter
	 * @param  ip   Description of the Parameter
	 */
	void changeState(ImagePlus imp, ImageProcessor ip) {
		scaleUpAndSet(ip, minThreshold, maxThreshold, minusHyst, plusHyst);
		updateScrollBars();
	}


	/**
	 *  Description of the Method
	 *
	 * @param  imp  Description of the Parameter
	 * @param  ip   Description of the Parameter
	 */
	void autoThreshold(ImagePlus imp, ImageProcessor ip) {
		ip.resetThreshold();
		previousImageID = 0;
		setup(imp);
	}



	/**
	 *  Multi thresholding for stack or image
	 *
	 * @param  T1        Low threshold
	 * @param  T2        High threshold
	 * @param  minhyst   Value for connected low pixels
	 * @param  plushyst  Value for connected high pixels
	 * @param  imaplus   Description of the Parameter
	 * @return           Description of the Return Value
	 */
	public ImagePlus doTrin(ImagePlus imaplus, double T1, double T2, int minhyst, int plushyst) {
		// stack
		int ns = imaplus.getStack().getSize();
		if (ns > 1) {
			ImageStack st = imaplus.getStack();
			ImageStack res = new ImageStack(st.getWidth(), st.getHeight());
			for (int s = 1; s <= ns; s++) {
				res.addSlice("", trin(st.getProcessor(s), T1, T2, minhyst, plushyst));
			}
			return new ImagePlus("Multi_Thresholding", res);
		} else {
			return new ImagePlus("Multi_Thresholding", trin(imaplus.getProcessor(), T1, T2, minhyst, plushyst));
		}
	}


	/**
	 *  Hysteresis thresholding for stack or image
	 *
	 * @param  T1        Low threshold
	 * @param  T2        High threshold
	 * @param  minhyst   Value for connected low pixels
	 * @param  plushyst  Value for connected high pixels
	 * @param  imaplus   Description of the Parameter
	 * @return           Description of the Return Value
	 */
	public ImagePlus doHyst(ImagePlus imaplus, double T1, double T2, int minhyst, int plushyst) {
		// stack
		boolean do3d = false;
		int ns = imaplus.getStack().getSize();
		if (ns > 1) {
				ImageStack st = imaplus.getStack();
				ImageStack res = new ImageStack(st.getWidth(), st.getHeight());
				for (int s = 1; s <= ns; s++) {
					res.addSlice("", hyst(st.getProcessor(s), T1, T2, minhyst, plushyst));
				}
				return new ImagePlus("Hysteresis_Thresholding", res);
		} else {
			return new ImagePlus("Hysteresis_Thresholding", hyst(imaplus.getProcessor(), T1, T2, minhyst, plushyst));
		}
	}


	/**
	 *  Multi thresholding
	 *
	 * @param  ima       Origonal image
	 * @param  T1        Low threshold
	 * @param  T2        High threshold
	 * @param  minhyst   Value for connected low pixels
	 * @param  plushyst  Value for connected high pixels
	 * @return           Description of the Return Value
	 */
	private ByteProcessor trin(ImageProcessor ima, double T1, double T2, int minhyst, int plushyst) {
		int la = ima.getWidth();
		int ha = ima.getHeight();
		ByteProcessor res = new ByteProcessor(la, ha);
		float pix;
		double min = scaleUp(ima, minThreshold);
		double max = scaleUp(ima, maxThreshold);

		for (int x = 0; x < la; x++) {
			for (int y = 0; y < ha; y++) {
				pix = ima.getPixelValue(x, y);
				if ((pix >= min) && (pix <= max)) {
					res.putPixelValue(x, y, 255);
				} else if ((pix >= min - minhyst) && (pix < min)) {
					res.putPixelValue(x, y, 100);
				} else if ((pix <= max + plushyst) && (pix > max)) {
					res.putPixelValue(x, y, 200);
				}
			}
		}
		return res;
	}


	/**
	 *  Hysteresis thresholding
	 *
	 * @param  ima       Orogonal image
	 * @param  T1        Low threshold
	 * @param  T2        High threshold
	 * @param  minhyst   Value for connected low pixels
	 * @param  plushyst  Value for connected high pixels
	 * @return           Description of the Return Value
	 */
	private ByteProcessor hyst(ImageProcessor ima, double T1, double T2, int minhyst, int plushyst) {
		int la = ima.getWidth();
		int ha = ima.getHeight();
		float pix;
		boolean change = true;

		ByteProcessor trin = new ByteProcessor(la, ha);
		double min = scaleUp(ima, minThreshold);
		double max = scaleUp(ima, maxThreshold);

		for (int x = 0; x < la; x++) {
			for (int y = 0; y < ha; y++) {
				pix = ima.getPixelValue(x, y);
				if ((pix >= min) && (pix <= max)) {
					trin.putPixelValue(x, y, 255);
				} else if (((pix >= min - minhyst) && (pix < min)) || ((pix <= max + plushyst) && (pix > max))) {
					trin.putPixelValue(x, y, 128);
				}
			}
		}

		ByteProcessor res = (ByteProcessor) trin.duplicate();
		// connection
		while (change) {
			change = false;
			for (int x = 1; x < la - 1; x++) {
				for (int y = 1; y < ha - 1; y++) {
					if (res.getPixelValue(x, y) == 255) {
						if (res.getPixelValue(x + 1, y) == 128) {
							change = true;
							res.putPixelValue(x + 1, y, 255);
						}
						if (res.getPixelValue(x - 1, y) == 128) {
							change = true;
							res.putPixelValue(x - 1, y, 255);
						}
						if (res.getPixelValue(x, y + 1) == 128) {
							change = true;
							res.putPixelValue(x, y + 1, 255);
						}
						if (res.getPixelValue(x, y - 1) == 128) {
							change = true;
							res.putPixelValue(x, y - 1, 255);
						}
						if (res.getPixelValue(x + 1, y + 1) == 128) {
							change = true;
							res.putPixelValue(x + 1, y + 1, 255);
						}
						if (res.getPixelValue(x - 1, y - 1) == 128) {
							change = true;
							res.putPixelValue(x - 1, y - 1, 255);
						}
						if (res.getPixelValue(x - 1, y + 1) == 128) {
							change = true;
							res.putPixelValue(x - 1, y + 1, 255);
						}
						if (res.getPixelValue(x + 1, y - 1) == 128) {
							change = true;
							res.putPixelValue(x + 1, y - 1, 255);
						}
					}
				}
			}
			if (change) {
				for (int x = la - 2; x > 0; x--) {
					for (int y = ha - 2; y > 0; y--) {
						if (res.getPixelValue(x, y) == 255) {
							if (res.getPixelValue(x + 1, y) == 128) {
								change = true;
								res.putPixelValue(x + 1, y, 255);
							}
							if (res.getPixelValue(x - 1, y) == 128) {
								change = true;
								res.putPixelValue(x - 1, y, 255);
							}
							if (res.getPixelValue(x, y + 1) == 128) {
								change = true;
								res.putPixelValue(x, y + 1, 255);
							}
							if (res.getPixelValue(x, y - 1) == 128) {
								change = true;
								res.putPixelValue(x, y - 1, 255);
							}
							if (res.getPixelValue(x + 1, y + 1) == 128) {
								change = true;
								res.putPixelValue(x + 1, y + 1, 255);
							}
							if (res.getPixelValue(x - 1, y - 1) == 128) {
								change = true;
								res.putPixelValue(x - 1, y - 1, 255);
							}
							if (res.getPixelValue(x - 1, y + 1) == 128) {
								change = true;
								res.putPixelValue(x - 1, y + 1, 255);
							}
							if (res.getPixelValue(x + 1, y - 1) == 128) {
								change = true;
								res.putPixelValue(x + 1, y - 1, 255);
							}
						}
					}
				}
			}
		}
		// suppression
		for (int x = 0; x < la; x++) {
			for (int y = 0; y < ha; y++) {
				if (res.getPixelValue(x, y) == 128) {
					res.putPixelValue(x, y, 0);
				}
			}
		}
		return res;
	}


	/**
	 *  Description of the Method
	 */
	void runThresholdCommand() {
		Recorder.recordInMacros = true;
		IJ.run("Convert to Mask");
		Recorder.recordInMacros = false;
	}


	final static int RESET = 0, AUTO = 1, HIST = 2, APPLY = 3, STATE_CHANGE = 4, MIN_THRESHOLD = 5, MAX_THRESHOLD = 6, SET = 7;
	final static int MINUS_HYST = 8, PLUS_HYST = 9;


	// Separate thread that does the potentially time-consuming processing
	/**
	 *  Main processing method for the ThresholdAdjuster object
	 */
	public void run() {
		while (!done) {
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {}
			}
			doUpdate();
		}
	}


	/**
	 *  Description of the Method
	 */
	void doUpdate() {
		//ImagePlus imp;
		//ImageProcessor ip;
		int action;
		int min = minValue;
		int max = maxValue;
		int minus = minusHystValue;
		int plus = plusHystValue;
		System.out.println("" + minusHystValue + " " + plusHystValue);
		if (doReset) {
			action = RESET;
		} else if (doAutoAdjust) {
			action = AUTO;
		} else if (doApplyLut) {
			action = APPLY;
		} else if (doStateChange) {
			action = STATE_CHANGE;
		} else if (doSet) {
			action = SET;
		} else if (minValue >= 0) {
			action = MIN_THRESHOLD;
		} else if (maxValue >= 0) {
			action = MAX_THRESHOLD;
		} else if (minusHystValue >= 0) {
			action = MINUS_HYST;
		} else if (plusHystValue >= 0) {
			action = PLUS_HYST;
		} else {
			return;
		}
		minValue = -1;
		maxValue = -1;
		minusHystValue = -1;
		plusHystValue = -1;
		doReset = false;
		doAutoAdjust = false;
		doApplyLut = false;
		doStateChange = false;
		doSet = false;
		/*
		 *  imp = WindowManager.getCurrentImage();
		 *  if (imp == null) {
		 *  IJ.beep();
		 *  IJ.showStatus("No image");
		 *  return;
		 *  }
		 *  ip = setup(imp);
		 *  if (ip == null) {
		 *  imp.unlock();
		 *  IJ.beep();
		 *  IJ.showStatus("RGB images cannot be thresholded");
		 *  return;
		 *  }
		 */
		//IJ.write("setup: "+(imp==null?"null":imp.getTitle()));
		switch (action) {
						case RESET:
							//reset(imp, ip);
							doHyst(impOrig, minThreshold, maxThreshold, minusHyst, plusHyst).show();
							break;
						case AUTO:
							autoThreshold(impOrig, ipOrig);
							break;
						case APPLY:
							// apply();
							doTrin(impOrig, minThreshold, maxThreshold, minusHyst, plusHyst).show();
							break;
						case STATE_CHANGE:
							changeState(impOrig, ipOrig);
							break;
						case SET:
							doSet(impOrig, ipOrig);
							break;
						case MIN_THRESHOLD:
							adjustMinThreshold(impOrig, ipOrig, min);
							break;
						case MAX_THRESHOLD:
							adjustMaxThreshold(impOrig, ipOrig, max);
							break;
						case MINUS_HYST:
							adjustMinusHyst(impOrig, ipOrig, minus);
							break;
						case PLUS_HYST:
							adjustPlusHyst(impOrig, ipOrig, plus);
							break;
		}
		updatePlot();
		updateLabels(impOrig, ipOrig);
		ipOrig.setLutAnimation(true);
		impOrig.updateAndDraw();
	}


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void windowClosing(WindowEvent e) {
		close();
	}


	/**
	 *  Overrides close() in PlugInFrame.
	 */
	public void close() {
		super.close();
		instance = null;
		done = true;
		synchronized (this) {
			notify();
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void windowActivated(WindowEvent e) {
		super.windowActivated(e);
		//ImagePlus imp = WindowManager.getCurrentImage();
		if (impOrig != null) {
			if (!firstActivation) {
				previousImageID = 0;
				setup(impOrig);
			}
			firstActivation = false;
		}
	}

}
// ThresholdAdjuster class

/**
 *  Description of the Class
 *
 * @author     thomas
 * @created    5 janvier 2007
 */
class ThresholdPlot extends Canvas implements Measurements, MouseListener {


	final static int WIDTH = 256, HEIGHT = 48;
	double minThreshold = 85;
	double maxThreshold = 170;
	int[] histogram;
	Color[] hColors;
	int hmax;
	Image os;
	Graphics osg;
	int mode;
	double stackMin, stackMax;


	/**
	 *  Constructor for the ThresholdPlot object
	 */
	public ThresholdPlot() {
		addMouseListener(this);
		setSize(WIDTH + 1, HEIGHT + 1);
	}


	/**
	 *  Overrides Component getPreferredSize(). Added to work around a bug in Java
	 *  1.4.1 on Mac OS X.
	 *
	 * @return    The preferredSize value
	 */
	public Dimension getPreferredSize() {
		return new Dimension(WIDTH + 1, HEIGHT + 1);
	}


	/**
	 *  Sets the histogram attribute of the ThresholdPlot object
	 *
	 * @param  imp                The new histogram value
	 * @param  useStackMinAndMax  The new histogram value
	 * @return                    Description of the Return Value
	 */
	ImageStatistics setHistogram(ImagePlus imp, boolean useStackMinAndMax) {
		ImageProcessor ip = imp.getProcessor();
		ImageStatistics stats = null;
		if (!(ip instanceof ByteProcessor)) {
			if (useStackMinAndMax) {
				stats = new StackStatistics(imp);
				if (imp.getLocalCalibration().isSigned16Bit()) {
					stats.min += 32768;
					stats.max += 32768;
				}
				stackMin = stats.min;
				stackMax = stats.max;
				ip.setMinAndMax(stackMin, stackMax);
			} else {
				stackMin = stackMax = 0.0;
			}
			Calibration cal = imp.getCalibration();
			if (ip instanceof FloatProcessor) {
				int digits = Math.max(Analyzer.getPrecision(), 2);
				IJ.showStatus("min=" + IJ.d2s(ip.getMin(), digits) + ", max=" + IJ.d2s(ip.getMax(), digits));
			} else {
				IJ.showStatus("min=" + (int) cal.getCValue(ip.getMin()) + ", max=" + (int) cal.getCValue(ip.getMax()));
			}
			ip = ip.convertToByte(true);
			ip.setColorModel(ip.getDefaultColorModel());
		}
		ip.setRoi(imp.getRoi());
		if (stats == null) {
			stats = ImageStatistics.getStatistics(ip, AREA + MIN_MAX + MODE, null);
		}
		int maxCount2 = 0;
		histogram = stats.histogram;
		for (int i = 0; i < stats.nBins; i++) {
			if ((histogram[i] > maxCount2) && (i != stats.mode)) {
				maxCount2 = histogram[i];
			}
		}
		hmax = stats.maxCount;
		if ((hmax > (maxCount2 * 2)) && (maxCount2 != 0)) {
			hmax = (int) (maxCount2 * 1.5);
			histogram[stats.mode] = hmax;
		}
		os = null;

		ColorModel cm = ip.getColorModel();
		if (!(cm instanceof IndexColorModel)) {
			return null;
		}
		IndexColorModel icm = (IndexColorModel) cm;
		int mapSize = icm.getMapSize();
		if (mapSize != 256) {
			return null;
		}
		byte[] r = new byte[256];
		byte[] g = new byte[256];
		byte[] b = new byte[256];
		icm.getReds(r);
		icm.getGreens(g);
		icm.getBlues(b);
		hColors = new Color[256];
		for (int i = 0; i < 256; i++) {
			hColors[i] = new Color(r[i] & 255, g[i] & 255, b[i] & 255);
		}
		return stats;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  g  Description of the Parameter
	 */
	public void update(Graphics g) {
		paint(g);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  g  Description of the Parameter
	 */
	public void paint(Graphics g) {
		if (g == null) {
			return;
		}
		if (histogram != null) {
			if (os == null && hmax > 0) {
				os = createImage(WIDTH, HEIGHT);
				osg = os.getGraphics();
				osg.setColor(Color.white);
				osg.fillRect(0, 0, WIDTH, HEIGHT);
				osg.setColor(Color.gray);
				for (int i = 0; i < WIDTH; i++) {
					if (hColors != null) {
						osg.setColor(hColors[i]);
					}
					osg.drawLine(i, HEIGHT, i, HEIGHT - ((int) (HEIGHT * histogram[i]) / hmax));
				}
				osg.dispose();
			}
			if (os == null) {
				return;
			}
			g.drawImage(os, 0, 0, this);
		} else {
			g.setColor(Color.white);
			g.fillRect(0, 0, WIDTH, HEIGHT);
		}
		g.setColor(Color.black);
		g.drawRect(0, 0, WIDTH, HEIGHT);
		if (mode == ConnectionThresholding_.RED) {
			g.setColor(Color.red);
		} else if (mode == ConnectionThresholding_.OVER_UNDER) {
			g.setColor(Color.blue);
			g.drawRect(1, 1, (int) minThreshold - 2, HEIGHT);
			g.drawRect(1, 0, (int) minThreshold - 2, 0);
			g.setColor(Color.green);
			g.drawRect((int) maxThreshold + 1, 1, WIDTH - (int) maxThreshold, HEIGHT);
			g.drawRect((int) maxThreshold + 1, 0, WIDTH - (int) maxThreshold, 0);
			return;
		}
		g.drawRect((int) minThreshold, 1, (int) (maxThreshold - minThreshold), HEIGHT);
		g.drawLine((int) minThreshold, 0, (int) maxThreshold, 0);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void mousePressed(MouseEvent e) { }


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void mouseReleased(MouseEvent e) { }


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void mouseExited(MouseEvent e) { }


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void mouseClicked(MouseEvent e) { }


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void mouseEntered(MouseEvent e) { }

}

