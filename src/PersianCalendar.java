import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import calendar.*;

public class PersianCalendar extends MIDlet {

	private Display display;

	// Screens of this MIDlet

	private Alert aboutAlert;

	private Form startScreen;

	private List dateConvList;

	private Form dateConvForm;

	private Form convResultScreen; // alert to show converted date
	
	private Alert convErrorAlert;

	// Common Commands
	private final Command backCommand; // used in many screens
	
	// Constants	
	private static final int PERSIAN = 0;
	
	private static final int ISLAMIC = 1;

	private static final int CIVIL = 2;

	public PersianCalendar() {
		display = Display.getDisplay(this);
		
		// main screen
		startScreen = new Form("\u0627\u0645\u0631\u0648\u0632");

		final Command exitCommand = new Command("\u062e\u0631\u0648\u062c", Command.EXIT, 0);
		final Command dateConvCommand = new Command(
				"\u062A\u0628\u062F\u064A\u0644",
				"\u062A\u0628\u062F\u064A\u0644\u0020\u062A\u0627\u0631\u064A\u062E",
				Command.SCREEN, 1);
		final Command aboutCommand = new Command("\u062F\u0631\u0628\u0627\u0631\u0647",
				Command.SCREEN, 1);

		startScreen.addCommand(exitCommand);
		startScreen.addCommand(dateConvCommand);
		startScreen.addCommand(aboutCommand);
		startScreen.setCommandListener(new CommandListener() {
			public void commandAction(Command c, Displayable d) {
				if (d == startScreen) {
					if (c == exitCommand)
						notifyDestroyed();

					if (c == aboutCommand)
						display.setCurrent(aboutAlert);

					if (c == dateConvCommand) {
						if (dateConvList == null)
							// lazy initialization
							createDateConvScreens();
						dateConvList.setSelectedIndex(0, true);
						display.setCurrent(dateConvList);
					}

					return;
				}
			}
		});

		// about screen
		aboutAlert = new Alert("\u062F\u0631\u0628\u0627\u0631\u0647");
		aboutAlert.setType(AlertType.INFO);
		aboutAlert.setTimeout(Alert.FOREVER);
		aboutAlert.setString(getAboutString());
		backCommand = new Command("\u0628\u0627\u0632\u06AF\u0634\u062A",
				Command.BACK, 1);
		aboutAlert.addCommand(backCommand);
		aboutAlert.setCommandListener(new CommandListener() {
			public void commandAction(Command c, Displayable d) {
				display.setCurrent(startScreen);
			}
		});
	}

	public void startApp() throws MIDletStateChangeException {
		// Today
		CivilDate civil = new CivilDate();
		PersianDate persian = DateConverter.civilToPersian(civil);
		IslamicDate islamic = DateConverter.civilToIslamic(civil);

		StringItem today = new StringItem(null, civil.getDayOfWeekName() + "\n" + persian + "\n"
				+ islamic + "\n" + civil);
		today.setLayout(Item.LAYOUT_2 | Item.LAYOUT_RIGHT);
		startScreen.append(today);

		display.setCurrent(startScreen);
	}

	public void pauseApp() {
	}

	public void destroyApp(boolean unconditional) {
	}

	private static final String getAboutString() {
		String s = "\u062A\u0642\u0648\u064A\u0645\u0020\u0641\u0627\u0631\u0633\u064A\n"
				+ "\u062A\u0648\u0633\u0639\u0647\u0020\u062F\u0647\u0646\u062F\u0647\u003A\u0020\u0627\u0645\u064A\u0631\u0631\u0636\u0627\u0020\u062E\u0633\u0631\u0648\u0634\u0627\u0647\u064A\n"
				+ "khosroshahi@\n"
				+ "ce.sharif.edu\n"
				+ "\u0627\u064A\u0646\u0020\u064A\u0643\u0020\u0646\u0631\u0645\u0020\u0627\u0641\u0632\u0627\u0631\u0020\u0622\u0632\u0627\u062F\u0020\u0627\u0633\u062A\u0021\n";
		return s;
	}

	private final void createDateConvScreens() {

		// Make date conv list, for selecting the source calendar
		dateConvList = new List(
				"\u062A\u0628\u062F\u064A\u0644\u0020\u0627\u0632",
				List.IMPLICIT);
		dateConvList.append("\u0634\u0645\u0633\u064A", null);
		dateConvList.append("\u0642\u0645\u0631\u064A", null);
		dateConvList.append("\u0645\u064A\u0644\u0627\u062F\u064A", null);
		final Command selectCommand = new Command("\u0627\u0646\u062a\u062e\u0627\u0628", Command.ITEM, 0);
		dateConvList.addCommand(selectCommand);
		dateConvList.addCommand(backCommand);
		dateConvList.setSelectCommand(selectCommand);
		dateConvList.setCommandListener(new CommandListener() {
			public void commandAction(Command c, Displayable d) {
				if (c == selectCommand) {
					customizeDateConvForm(dateConvList.getSelectedIndex());
					display.setCurrentItem(dateConvForm.get(0));
				} else { // back command
					display.setCurrent(startScreen);
				}
			}
		});

		// Make date conversion form
		dateConvForm = new Form("");

		TextField yearField = new TextField("\u0633\u0627\u0644", "", 5,
				TextField.NUMERIC);
		yearField.setLayout(Item.LAYOUT_RIGHT);
		dateConvForm.append(yearField);

		TextField monthField = new TextField("\u0645\u0627\u0647", "", 2,
				TextField.NUMERIC);
		monthField.setLayout(Item.LAYOUT_RIGHT);
		dateConvForm.append(monthField);

		TextField dayField = new TextField("\u0631\u0648\u0632", "", 2,
				TextField.NUMERIC);
		dayField.setLayout(Item.LAYOUT_RIGHT);
		dateConvForm.append(dayField);

		final Command convertCommand = new Command(
				"\u062A\u0628\u062F\u064A\u0644", Command.OK, 1);
		dateConvForm.addCommand(backCommand);
		dateConvForm.addCommand(convertCommand);

		dateConvForm.setCommandListener(new CommandListener() {
			public void commandAction(Command c, Displayable d) {
				if (c == backCommand)
					display.setCurrent(dateConvList);
				else if (c == convertCommand)
					convertDate();
			}
		});
		
		// Prefill the text fields with the date of today
		/*
		AbstractDate today = null;
		switch(selIndex) {
		case PERSIAN:
			today = DateConverter.civilToPersian(new CivilDate());
			yearField.setString(today.getYear() + "");
			monthField.setString(today.getMonth() + "");
			dayField.setString(today.getDayOfMonth() + "");
			break;
		case ISLAMIC:
			today = DateConverter.civilToIslamic(new CivilDate());
			yearField.setString(today.getYear() + "");
			monthField.setString(today.getMonth() + "");
			dayField.setString(today.getDayOfMonth() + "");
			break;
		case CIVIL:
			today = new CivilDate();
			yearField.setString(today.getYear() + "");
			monthField.setString(today.getMonth() + "");
			dayField.setString(today.getDayOfMonth() + "");
			break;
		}
		*/
		
		// Make the alert that shows the converted date
		convResultScreen = new Form("\u062A\u0628\u062F\u064A\u0644");
		final Command againCommand = new Command(
				"\u062F\u0648\u0628\u0627\u0631\u0647", Command.SCREEN, 1);
		final Command finishCommand = new Command(
				"\u067E\u0627\u064A\u0627\u0646", Command.OK, 1);
		convResultScreen.addCommand(againCommand);
		convResultScreen.addCommand(finishCommand);
		convResultScreen.setCommandListener(new CommandListener() {
			public void commandAction(Command c, Displayable d) {
				if (c == againCommand)
					display.setCurrent(dateConvList);
				else if (c == finishCommand)
					display.setCurrent(startScreen);
			}
		});
		StringItem si = new StringItem(null, "");
		si.setLayout(Item.LAYOUT_2 | Item.LAYOUT_RIGHT);
		convResultScreen.append(si);
		
		// Make the warning alert displayed when entering
		// invalid dates
		convErrorAlert = new Alert("\u062E\u0637\u0627"/*khata*/);
		convErrorAlert.setType(AlertType.ERROR);
		
	}

	private final void convertDate() {
		// TODO try/catch convertion method calls
		final int YEAR = 0, MONTH = 1, DAY = 2, ERROR = 3;
		
		int year = 1, month = 1, day = 1;
		
		// validate year
		try {
			year = Integer.parseInt(((TextField) dateConvForm.get(0))
					.getString());
		} catch(NumberFormatException e) {
			showConvErrorAlert(YEAR);
			return;
		}
		
		// validate month
		try {
			month = Integer.parseInt(((TextField) dateConvForm.get(1))
					.getString());			
		} catch (NumberFormatException e) {
			showConvErrorAlert(MONTH);
			return;
		}
		
		// validate day
		try {
			day = Integer.parseInt(((TextField) dateConvForm.get(2))
					.getString());			
		} catch (Exception e) {
			showConvErrorAlert(DAY);
			return;
		}

		CivilDate civil = null;
		IslamicDate islamic = null;
		PersianDate persian = null;
		String result = null;

		switch (dateConvList.getSelectedIndex()) {
		case PERSIAN:
			try {
				persian = new PersianDate(year, month, day);
			} catch (YearOutOfRangeException e) {
				showConvErrorAlert(YEAR);
				return;
			} catch (MonthOutOfRangeException e) {
				showConvErrorAlert(MONTH);
				return;
			} catch (DayOutOfRangeException e) {
				showConvErrorAlert(DAY);
				return;
			}
			
			try {
				civil = DateConverter.persianToCivil(persian);
				islamic = DateConverter.persianToIslamic(persian);				
			} catch (Exception e) {
				showConvErrorAlert(ERROR);
			}
			
			result = "\u062A\u0627\u0631\u064A\u062E\u0020\u0634\u0645\u0633\u064A:\n"
					+ persian
					+ "\n"
					+ "\u0628\u0631\u0627\u0628\u0631\u0020\u0642\u0645\u0631\u064A:\n"
					+ islamic
					+ "\n"
					+ "\u0628\u0631\u0627\u0628\u0631\u0020\u0645\u064A\u0644\u0627\u062F\u064A:\n"
					+ civil;
			break;

		case ISLAMIC:
			try {
				islamic = new IslamicDate(year, month, day);
			} catch (YearOutOfRangeException e) {
				showConvErrorAlert(YEAR);
				return;
			} catch (MonthOutOfRangeException e) {
				showConvErrorAlert(MONTH);
				return;
			} catch (DayOutOfRangeException e) {
				showConvErrorAlert(DAY);
				return;
			}

			try {
				persian = DateConverter.islamicToPersian(islamic);
				civil = DateConverter.islamicToCivil(islamic);				
			} catch (Exception e) {
				showConvErrorAlert(ERROR);
			}
			
			result = "\u062A\u0627\u0631\u064A\u062E\u0020\u0645\u064A\u0644\u0627\u062F\u064A:\n"
					+ islamic
					+ "\n"
					+ "\u0628\u0631\u0627\u0628\u0631\u0020\u0634\u0645\u0633\u064A:\n"
					+ persian
					+ "\n"
					+ "\u0628\u0631\u0627\u0628\u0631\u0020\u0645\u064A\u0644\u0627\u062F\u064A:\n"
					+ civil;
			break;
		case CIVIL:
			try {
				civil = new CivilDate(year, month, day);
			} catch (YearOutOfRangeException e) {
				showConvErrorAlert(YEAR);
				return;
			} catch (MonthOutOfRangeException e) {
				showConvErrorAlert(MONTH);
				return;
			} catch (DayOutOfRangeException e) {
				showConvErrorAlert(DAY);
				return;
			}

			try {
				persian = DateConverter.civilToPersian(civil);
				islamic = DateConverter.civilToIslamic(civil);				
			} catch (Exception e) {
				showConvErrorAlert(ERROR);
			}
			
			result = "\u062A\u0627\u0631\u064A\u062E\u0020\u0645\u064A\u0644\u0627\u062F\u064A:\n"
					+ civil
					+ "\n"
					+ "\u0628\u0631\u0627\u0628\u0631\u0020\u0634\u0645\u0633\u064A:\n"
					+ persian
					+ "\n"
					+ "\u0628\u0631\u0627\u0628\u0631\u0020\u0642\u0645\u0631\u064A:\n"
					+ islamic;
			break;
		}
		
		result += "\n\u0631\u0648\u0632\u0020\u0647\u0641\u062A\u0647:\n"
				+ civil.getDayOfWeekName();
		((StringItem)convResultScreen.get(0)).setText(result);
		display.setCurrent(convResultScreen);
	}
	
	private final void showConvErrorAlert(int type) {
		if( type < 3) {
			// Invalid year, month or day
			// = "vared shode mo'taber nist"
			String strInvalid = "\u0648\u0627\u0631\u062F\u0020\u0634\u062F\u0647\u0020\u0645\u0639\u062A\u0628\u0631\u0020\u0646\u064A\u0633\u062A\u0021";
			// = "saal", "maah", "rooz"
			String[] YMD = {"\u0633\u0627\u0644 ",
					"\u0645\u0627\u0647 ", "\u0631\u0648\u0632 "};
			convErrorAlert.setString(YMD[type] + strInvalid);
			display.setCurrent(convErrorAlert);
			display.setCurrentItem(dateConvForm.get(type));
		} else { // exception in conversion
			convErrorAlert.setString("\u062E\u0637\u0627\u0020\u062F\u0631\u0020\u062A\u0628\u062F\u064A\u0644\u0021");
			display.setCurrent(convErrorAlert, dateConvForm);
		}
	}

	private final void customizeDateConvForm(int selectedIndex) {
		switch (selectedIndex) {
		case PERSIAN:
			dateConvForm
					.setTitle("\u062A\u0627\u0631\u064A\u062E\u0020\u0634\u0645\u0633\u064A");
			break;
		case ISLAMIC:
			dateConvForm
					.setTitle("\u062A\u0627\u0631\u064A\u062E\u0020\u0642\u0645\u0631\u064A");
			break;
		case CIVIL:
			dateConvForm
					.setTitle("\u062A\u0627\u0631\u064A\u062E\u0020\u0645\u064A\u0644\u0627\u062F\u064A");
			break;
		}
	}

}
