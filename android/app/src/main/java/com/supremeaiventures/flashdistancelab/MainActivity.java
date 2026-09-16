package com.supremeaiventures.flashdistancelab;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int INK = Color.rgb(20, 20, 22);
    private static final int MUTED = Color.rgb(98, 101, 108);
    private static final int PAPER = Color.rgb(247, 244, 238);
    private static final int PANEL = Color.WHITE;
    private static final int LINE = Color.rgb(218, 216, 210);
    private static final int AMBER = Color.rgb(215, 106, 34);
    private static final int AMBER_PALE = Color.rgb(255, 226, 173);
    private static final int GREEN = Color.rgb(77, 139, 87);
    private static final String[] POWER_LABELS = {"1/128", "1/64", "1/32", "1/16", "1/8", "1/4", "1/2", "1/1"};

    private final List<Button> distanceButtons = new ArrayList<>();
    private final List<Button> powerButtons = new ArrayList<>();

    private FlashSceneView sceneView;
    private MeterView meterView;
    private SeekBar distanceSeekBar;
    private SeekBar powerSeekBar;
    private TextView distanceValue;
    private TextView powerValue;
    private TextView relativeLight;
    private TextView stopShift;
    private TextView exposureTone;
    private TextView apertureValue;
    private TextView matchPower;
    private TextView matchPowerNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureSystemBars();

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(PAPER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        }
        scrollView.setOnApplyWindowInsetsListener((view, insets) -> {
            int topInset;
            int bottomInset;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                int systemBars = WindowInsets.Type.systemBars();
                topInset = insets.getInsets(systemBars).top;
                bottomInset = insets.getInsets(systemBars).bottom;
            } else {
                topInset = insets.getSystemWindowInsetTop();
                bottomInset = insets.getSystemWindowInsetBottom();
            }
            view.setPadding(view.getPaddingLeft(), topInset, view.getPaddingRight(), bottomInset);
            return insets;
        });

        LinearLayout content = vertical();
        content.setPadding(dp(16), dp(12), dp(16), dp(24));
        scrollView.addView(content, new ScrollView.LayoutParams(-1, -2));
        setContentView(scrollView);

        addHeader(content);
        addHero(content);
        addLab(content);
        addReadouts(content);
        addLesson(content);
        addFooter(content);

        wireControls();
        updateSimulation();
    }

    private void configureSystemBars() {
        Window window = getWindow();
        window.setStatusBarColor(PAPER);
        window.setNavigationBarColor(PAPER);
        int flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        window.getDecorView().setSystemUiVisibility(flags);
    }

    private void addHeader(LinearLayout content) {
        LinearLayout header = horizontal();
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(12));

        TextView brand = text("Flash Distance Lab", 17, INK, true);
        header.addView(brand, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView formula = pill("power / distance^2", INK, Color.TRANSPARENT, LINE);
        header.addView(formula, wrapParams());
        content.addView(header, matchWidthParams());
        content.addView(divider(), new LinearLayout.LayoutParams(-1, dp(1)));
    }

    private void addHero(LinearLayout content) {
        addTopSpace(content, 30);
        content.addView(text("INVERSE-SQUARE FLASH EXPOSURE", 12, AMBER, true), wrapParams());

        TextView title = text("Distance doubles. Light becomes one quarter.", 34, INK, true);
        title.setLineSpacing(0f, 0.94f);
        addTopSpace(content, 6);
        content.addView(title, wrapParams());

        TextView lead = text("Move the light and change flash power to see how exposure changes at the subject.", 16, MUTED, false);
        lead.setLineSpacing(0f, 1.25f);
        addTopSpace(content, 10);
        content.addView(lead, wrapParams());

        LinearLayout formulaRow = horizontal();
        formulaRow.setGravity(Gravity.CENTER_VERTICAL);
        addTopSpace(content, 14);
        formulaRow.addView(pill("subject light", INK, PANEL, LINE), wrapParams());
        formulaRow.addView(text("=", 16, MUTED, true), marginParams(8, 0, 8, 0));
        formulaRow.addView(pill("flash power", INK, PANEL, LINE), wrapParams());
        formulaRow.addView(text("/", 16, MUTED, true), marginParams(8, 0, 8, 0));
        formulaRow.addView(pill("distance^2", INK, PANEL, LINE), wrapParams());
        content.addView(formulaRow, matchWidthParams());
    }

    private void addLab(LinearLayout content) {
        addTopSpace(content, 34);
        content.addView(text("INTERACTIVE SIMULATOR", 12, AMBER, true), wrapParams());

        TextView heading = text("Move distance and power together.", 23, INK, true);
        addTopSpace(content, 6);
        content.addView(heading, wrapParams());

        TextView description = text("The stand moves across the studio while the subject stays fixed. The beam keeps a 30-degree angle.", 14, MUTED, false);
        description.setLineSpacing(0f, 1.25f);
        addTopSpace(content, 8);
        content.addView(description, wrapParams());

        LinearLayout sceneCard = card(false);
        addTopSpace(content, 14);
        sceneView = new FlashSceneView(this, null);
        sceneCard.addView(sceneView, new LinearLayout.LayoutParams(-1, -2));
        meterView = new MeterView(this, null);
        sceneCard.addView(meterView, new LinearLayout.LayoutParams(-1, dp(52)));
        LinearLayout meterLabels = horizontal();
        meterLabels.setPadding(dp(16), 0, dp(16), dp(10));
        String[] stops = {"-4", "-3", "-2", "-1", "0", "+1", "+2", "+3", "+4"};
        for (String stop : stops) {
            TextView label = text(stop, 10, MUTED, true);
            label.setGravity(Gravity.CENTER);
            meterLabels.addView(label, new LinearLayout.LayoutParams(0, -2, 1f));
        }
        sceneCard.addView(meterLabels, new LinearLayout.LayoutParams(-1, -2));
        content.addView(sceneCard, matchWidthParams());

        LinearLayout controlsCard = card(true);
        addTopSpace(content, 12);
        addDistanceControls(controlsCard);
        addPowerControls(controlsCard);
        content.addView(controlsCard, matchWidthParams());
    }

    private void addDistanceControls(LinearLayout controlsCard) {
        LinearLayout labelRow = horizontal();
        labelRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView label = text("Distance", 16, INK, true);
        labelRow.addView(label, new LinearLayout.LayoutParams(0, -2, 1f));
        distanceValue = text("4.0 ft", 22, AMBER, true);
        distanceValue.setGravity(Gravity.RIGHT);
        labelRow.addView(distanceValue, wrapParams());
        controlsCard.addView(labelRow, matchWidthParams());

        distanceSeekBar = new SeekBar(this);
        distanceSeekBar.setMax(56);
        distanceSeekBar.setProgress(8);
        addTopSpace(controlsCard, 2);
        controlsCard.addView(distanceSeekBar, new LinearLayout.LayoutParams(-1, dp(36)));

        LinearLayout scale = horizontal();
        scale.addView(text("2 ft", 11, MUTED, true), new LinearLayout.LayoutParams(0, -2, 1f));
        scale.addView(text("4 ft", 11, MUTED, true), new LinearLayout.LayoutParams(0, -2, 1f));
        scale.addView(text("8 ft", 11, MUTED, true), new LinearLayout.LayoutParams(0, -2, 1f));
        TextView sixteen = text("16 ft", 11, MUTED, true);
        sixteen.setGravity(Gravity.RIGHT);
        scale.addView(sixteen, new LinearLayout.LayoutParams(0, -2, 1f));
        controlsCard.addView(scale, matchWidthParams());

        LinearLayout presets = horizontal();
        int[] values = {2, 4, 8, 16};
        for (int index = 0; index < values.length; index++) {
            final int distance = values[index];
            Button button = choiceButton(String.valueOf(distance));
            button.setOnClickListener(view -> {
                distanceSeekBar.setProgress(Math.round((distance - 2f) / 0.25f));
                updateSimulation();
            });
            distanceButtons.add(button);
            presets.addView(button, weightedButtonParams(index > 0));
        }
        addTopSpace(controlsCard, 8);
        controlsCard.addView(presets, matchWidthParams());
    }

    private void addPowerControls(LinearLayout controlsCard) {
        addTopSpace(controlsCard, 20);
        controlsCard.addView(divider(), new LinearLayout.LayoutParams(-1, dp(1)));
        addTopSpace(controlsCard, 16);

        LinearLayout labelRow = horizontal();
        labelRow.setGravity(Gravity.CENTER_VERTICAL);
        labelRow.addView(text("Flash power", 16, INK, true), new LinearLayout.LayoutParams(0, -2, 1f));
        powerValue = text("1/16", 22, AMBER, true);
        powerValue.setGravity(Gravity.RIGHT);
        labelRow.addView(powerValue, wrapParams());
        controlsCard.addView(labelRow, matchWidthParams());

        powerSeekBar = new SeekBar(this);
        powerSeekBar.setMax(7);
        powerSeekBar.setProgress(3);
        addTopSpace(controlsCard, 2);
        controlsCard.addView(powerSeekBar, new LinearLayout.LayoutParams(-1, dp(36)));

        LinearLayout scale = horizontal();
        scale.addView(text("1/128", 11, MUTED, true), new LinearLayout.LayoutParams(0, -2, 1f));
        TextView middle = text("1/16", 11, MUTED, true);
        middle.setGravity(Gravity.CENTER);
        scale.addView(middle, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView full = text("1/1", 11, MUTED, true);
        full.setGravity(Gravity.RIGHT);
        scale.addView(full, new LinearLayout.LayoutParams(0, -2, 1f));
        controlsCard.addView(scale, matchWidthParams());

        addPowerButtonRow(controlsCard, 0, 4);
        addPowerButtonRow(controlsCard, 4, 4);
    }

    private void addPowerButtonRow(LinearLayout controlsCard, int start, int count) {
        LinearLayout row = horizontal();
        for (int index = 0; index < count; index++) {
            int powerStop = start + index - 7;
            Button button = choiceButton(POWER_LABELS[start + index]);
            button.setTextSize(13);
            button.setOnClickListener(view -> {
                powerSeekBar.setProgress(powerStop + 7);
                updateSimulation();
            });
            powerButtons.add(button);
            row.addView(button, weightedButtonParams(index > 0));
        }
        addTopSpace(controlsCard, 8);
        controlsCard.addView(row, matchWidthParams());
    }

    private void addReadouts(LinearLayout content) {
        addTopSpace(content, 24);
        content.addView(text("CURRENT READOUT", 12, AMBER, true), wrapParams());

        LinearLayout grid = vertical();
        addTopSpace(content, 8);
        grid.addView(readoutRow(
                readoutCard("Subject light", "1.00x", "relative to 4 ft at 1/16 power"),
                readoutCard("Exposure shift", "0.0 stops", "same as reference")), wrapParams());
        grid.addView(readoutRow(
                readoutCard("Meter aperture", "f/5.6", "same ISO and shutter speed"),
                readoutCard("Power to match reference", "1/16", "at the selected distance")), marginParams(0, 8, 0, 0));
        content.addView(grid, matchWidthParams());
    }

    private LinearLayout readoutRow(LinearLayout first, LinearLayout second) {
        LinearLayout row = horizontal();
        row.addView(first, new LinearLayout.LayoutParams(0, dp(124), 1f));
        LinearLayout.LayoutParams secondParams = new LinearLayout.LayoutParams(0, dp(124), 1f);
        secondParams.setMargins(dp(8), 0, 0, 0);
        row.addView(second, secondParams);
        return row;
    }

    private LinearLayout readoutCard(String label, String value, String note) {
        LinearLayout card = card(true);
        card.setPadding(dp(12), dp(11), dp(12), dp(10));
        card.addView(text(label, 10, MUTED, true), wrapParams());
        TextView valueView = text(value, 23, INK, true);
        addTopSpace(card, 10);
        card.addView(valueView, wrapParams());
        TextView noteView = text(note, 11, MUTED, false);
        addTopSpace(card, 6);
        card.addView(noteView, wrapParams());

        if ("Subject light".equals(label)) relativeLight = valueView;
        if ("Exposure shift".equals(label)) {
            stopShift = valueView;
            exposureTone = noteView;
        }
        if ("Meter aperture".equals(label)) apertureValue = valueView;
        if ("Power to match reference".equals(label)) {
            matchPower = valueView;
            matchPowerNote = noteView;
        }
        return card;
    }

    private void addLesson(LinearLayout content) {
        addTopSpace(content, 34);
        content.addView(text("DISTANCE RATIO", 12, GREEN, true), wrapParams());
        TextView title = text("Every doubling costs two stops.", 23, INK, true);
        addTopSpace(content, 6);
        content.addView(title, wrapParams());

        addRatioCard(content, "Distance x2", "1/4 light", "add x4 power, or +2 stops");
        addRatioCard(content, "Distance x4", "1/16 light", "add x16 power, or +4 stops");
        addRatioCard(content, "Distance x0.5", "x4 light", "reduce power by two stops");
    }

    private void addRatioCard(LinearLayout content, String label, String value, String note) {
        LinearLayout ratio = card(true);
        ratio.setPadding(dp(14), dp(13), dp(14), dp(13));
        addTopSpace(content, 8);
        ratio.addView(text(label, 10, MUTED, true), wrapParams());
        TextView valueView = text(value, 27, INK, true);
        addTopSpace(ratio, 14);
        ratio.addView(valueView, wrapParams());
        TextView noteView = text(note, 12, MUTED, false);
        addTopSpace(ratio, 8);
        ratio.addView(noteView, wrapParams());
        content.addView(ratio, matchWidthParams());
    }

    private void addFooter(LinearLayout content) {
        addTopSpace(content, 30);
        TextView footer = text("©2026 SUPREME AI VENTURES LLC", 11, MUTED, true);
        footer.setGravity(Gravity.CENTER);
        content.addView(footer, wrapParams());
    }

    private void wireControls() {
        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSimulation();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        powerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSimulation();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void updateSimulation() {
        if (distanceSeekBar == null || powerSeekBar == null) return;

        float distance = 2f + distanceSeekBar.getProgress() * 0.25f;
        int powerStop = powerSeekBar.getProgress() - 7;
        double referenceDistance = 4d;
        double referencePower = Math.pow(2d, -4d);
        double power = Math.pow(2d, powerStop);
        double distanceOnly = Math.pow(referenceDistance / distance, 2d);
        double relativeExposureValue = (power / referencePower) * distanceOnly;
        double stopValue = Math.log(relativeExposureValue) / Math.log(2d);
        double aperture = 5.6d * Math.sqrt(relativeExposureValue);
        double neededPower = referencePower * Math.pow(distance / referenceDistance, 2d);
        double neededPowerStops = Math.log(neededPower) / Math.log(2d);

        String distanceText = String.format(Locale.US, "%.1f ft", distance);
        distanceValue.setText(distanceText);
        powerValue.setText(powerLabel(powerStop));
        relativeLight.setText(formatMultiplier(relativeExposureValue));
        stopShift.setText(formatStops(stopValue));
        apertureValue.setText(formatAperture(aperture));
        matchPower.setText(formatNeededPower(neededPower));

        if (Math.abs(stopValue) < 0.15d) {
            exposureTone.setText("same as reference");
        } else if (stopValue > 0d) {
            exposureTone.setText("brighter than reference");
        } else {
            exposureTone.setText("darker than reference");
        }

        String matchNote = formatStops(neededPowerStops + 4d) + " from reference";
        if (neededPowerStops > 0.05d) matchNote += ", beyond full power";
        matchPowerNote.setText(matchNote);

        sceneView.setSimulation(distance, powerStop, relativeExposureValue, stopValue);
        meterView.setStops(stopValue);
        updateButtonStyles(distance, powerStop);
    }

    private void updateButtonStyles(float distance, int powerStop) {
        int[] distances = {2, 4, 8, 16};
        for (int index = 0; index < distanceButtons.size(); index++) {
            boolean active = Math.abs(distance - distances[index]) < 0.01f;
            styleChoiceButton(distanceButtons.get(index), active, false);
        }
        for (int index = 0; index < powerButtons.size(); index++) {
            boolean active = (index - 7) == powerStop;
            styleChoiceButton(powerButtons.get(index), active, true);
        }
    }

    private static String formatMultiplier(double value) {
        if (Math.abs(value - 1d) < 0.005d) return "1.00x";
        if (value > 1d) return trim(value, value >= 10d ? 1 : 2) + "x";
        double inverse = 1d / value;
        if (inverse <= 16d) return "1/" + trim(inverse, inverse >= 10d ? 1 : 2);
        return trim(value, 3) + "x";
    }

    private static String formatStops(double value) {
        if (Math.abs(value) < 0.05d) return "0.0 stops";
        return String.format(Locale.US, "%+.1f stops", value);
    }

    private static String formatAperture(double value) {
        if (value < 10d) return String.format(Locale.US, "f/%.1f", value);
        return String.format(Locale.US, "f/%d", Math.round(value));
    }

    private static String formatNeededPower(double fraction) {
        double exactStop = Math.log(fraction) / Math.log(2d);
        int roundedStop = (int) Math.round(exactStop);
        double roundedFraction = Math.pow(2d, roundedStop);
        if (Math.abs(fraction - roundedFraction) < 0.012d && roundedStop >= -7 && roundedStop <= 0) {
            return powerLabel(roundedStop);
        }
        if (fraction > 1d) return trim(fraction, 2) + "x full";
        return Math.round(fraction * 100d) + "% full";
    }

    private static String trim(double value, int digits) {
        return String.format(Locale.US, "%." + digits + "f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    static String powerLabel(int powerStop) {
        int index = powerStop + 7;
        if (index < 0 || index >= POWER_LABELS.length) return "beyond full";
        return POWER_LABELS[index];
    }

    private TextView text(String value, int sizeSp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        view.setIncludeFontPadding(false);
        return view;
    }

    private TextView pill(String value, int color, int fill, int stroke) {
        TextView view = text(value, 12, color, false);
        view.setPadding(dp(10), dp(7), dp(10), dp(7));
        view.setBackground(background(fill, stroke));
        return view;
    }

    private Button choiceButton(String value) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextSize(14);
        button.setTextColor(INK);
        button.setAllCaps(false);
        button.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setPadding(dp(4), 0, dp(4), 0);
        button.setStateListAnimator(null);
        styleChoiceButton(button, false, false);
        return button;
    }

    private void styleChoiceButton(Button button, boolean active, boolean power) {
        button.setTextColor(active && !power ? Color.WHITE : (active ? Color.rgb(102, 49, 13) : INK));
        button.setBackground(background(active && !power ? INK : (active ? AMBER_PALE : Color.rgb(237, 232, 223)),
                active ? AMBER : Color.TRANSPARENT));
    }

    private LinearLayout vertical() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout horizontal() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        return layout;
    }

    private LinearLayout card(boolean padded) {
        LinearLayout layout = vertical();
        layout.setBackground(background(PANEL, LINE));
        layout.setElevation(dp(3));
        if (padded) layout.setPadding(dp(16), dp(15), dp(16), dp(15));
        return layout;
    }

    private View divider() {
        View view = new View(this);
        view.setBackgroundColor(LINE);
        return view;
    }

    private android.graphics.drawable.GradientDrawable background(int fill, int stroke) {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(8));
        if (stroke != Color.TRANSPARENT) drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private LinearLayout.LayoutParams wrapParams() {
        return new LinearLayout.LayoutParams(-2, -2);
    }

    private LinearLayout.LayoutParams matchWidthParams() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private LinearLayout.LayoutParams marginParams(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = wrapParams();
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private LinearLayout.LayoutParams weightedButtonParams(boolean separated) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(42), 1f);
        if (separated) params.setMargins(dp(6), 0, 0, 0);
        return params;
    }

    private void addTopSpace(LinearLayout parent, int size) {
        View spacer = new View(this);
        parent.addView(spacer, new LinearLayout.LayoutParams(1, dp(size)));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
