package com.kabc.desktop.date;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class DateTextField extends TextField {

    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final String INIT_DATE = "__.__.____";
    private static final List<Integer> DELIMITERS = Arrays.asList(2, 5);

    enum Unit { DAYS, MONTHS, YEARS }

    public DateTextField() {
        this(INIT_DATE);
        addEventFilter(KeyEvent.KEY_TYPED, event -> {
            int pos = getCaretPosition();
            if (pos > INIT_DATE.length() - 1 || !"0123456789".contains(event.getCharacter().toLowerCase())) {
                event.consume();
            }
        });
        addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            int pos = getCaretPosition();
            if (DELIMITERS.contains(pos) && event.getCode() != KeyCode.LEFT && event.getCode() != KeyCode.BACK_SPACE) {
                forward();
                event.consume();
            }
        });
    }

    public DateTextField(String date) {
        super(date);

        ReadOnlyStringWrapper years = new ReadOnlyStringWrapper(this, "years");
        ReadOnlyStringWrapper months = new ReadOnlyStringWrapper(this, "months");
        ReadOnlyStringWrapper days = new ReadOnlyStringWrapper(this, "days");

        years.bind(new DateTextField.DateUnitBinding(Unit.YEARS));
        months.bind(new DateTextField.DateUnitBinding(Unit.MONTHS));
        days.bind(new DateTextField.DateUnitBinding(Unit.DAYS));
    }

    @Override
    public void appendText(String text) {}

    @Override
    public boolean deleteNextChar() {
        boolean success = false;
        final IndexRange selection = getSelection();
        if (selection.getLength() > 0) {
            int selectionEnd = selection.getEnd();
            deleteText(selection);
            positionCaret(selectionEnd);
            success = true;
        } else {
            int pos = getCaretPosition();
            if (!DELIMITERS.contains(pos)) {
                String currentText = getText();
                setText(currentText.substring(0, pos) + "_" + currentText.substring(pos + 1));
                success = true;
            }
            positionCaret(Math.min(pos + 1, getText().length()));
        }
        return success;
    }

    @Override
    public boolean deletePreviousChar() {
        boolean success = false;
        final IndexRange selection = getSelection();
        if (selection.getLength() > 0) {
            int selectionStart = selection.getStart();
            deleteText(selection);
            positionCaret(selectionStart);
            success = true;
        } else {
            int pos = getCaretPosition();
            if (pos != 0 && pos != DELIMITERS.get(0) + 1 && pos != DELIMITERS.get(1) + 1) {
                String currentText = getText();
                setText(currentText.substring(0, pos - 1) + "_" + currentText.substring(pos));
                success = true;
            }
            positionCaret(Math.max(pos - 1, 0));
        }
        return success;
    }

    @Override
    public void deleteText(IndexRange range) {
        deleteText(range.getStart(), range.getEnd());
    }

    @Override
    public void deleteText(int begin, int end) {
        StringBuilder builder = new StringBuilder(getText());
        for (int pos = begin; pos < end; pos++) {
            if (!DELIMITERS.contains(pos)) {
                builder.replace(pos, pos + 1, "_");
            }
        }
        setText(builder.toString());
    }

    @Override
    public void insertText(int index, String text) {
        replaceText(index, index + text.length(), text);
    }

    @Override
    public void replaceSelection(String replacement) {
        final IndexRange selection = getSelection();
        if (selection.getLength() == 0) {
            insertText(selection.getStart(), replacement);
        } else {
            replaceText(selection, replacement);
        }
    }

    @Override
    public void replaceText(IndexRange range, String text) {
        replaceText(range.getStart(), range.getEnd(), text);
    }

    @Override
    public void replaceText(int begin, int end, String text) {
        if (begin == end) {
            insertText(begin, text);
            return;
        }
        end = Math.min(end, INIT_DATE.length());
        String completedText = completeDateString(begin, end, text);
        StringBuilder builder = new StringBuilder(getText());
        builder.replace(begin, end, completedText);
        String newText = builder.toString();
        if (isValidDate(newText)) {
            setText(newText);
            positionCaret(Math.min(end, begin + text.length()));
        } else {
            positionCaret(begin);
        }
    }

    private String completeDateString(int begin, int end, String text) {
        String completedDateString = text;
        int textEnd = begin + text.length();
        if (end < textEnd) {
            completedDateString = text.substring(0, end - begin);
        } else if (end > textEnd) {
            StringBuilder builder = new StringBuilder(INIT_DATE);
            builder.replace(begin, textEnd, text);
            completedDateString = builder.substring(begin, end);
        }
        return completedDateString;
    }

    private boolean isValidDate(String date) {
        return Pattern.compile("[\\d|_]{2}\\.[\\d|_]{2}\\.[\\d|_]{4}").matcher(date).matches();
    }

    private final class DateUnitBinding extends StringBinding {

        final Unit unit;

        DateUnitBinding(Unit unit) {
            bind(textProperty());
            this.unit = unit;
        }

        @Override
        protected String computeValue() {
            return getText().split("\\.")[unit.ordinal()];
        }
    }
}