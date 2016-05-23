package it.piemonte.arpa.openoise;

/**
 * Created by stefmase on 16/04/2015.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PlotThirdOctave extends View {

    private Paint paintLines2, paintLabelsY, paintLabelsX, paintLabelsMax,paintLabelsMaxRectFill,paintLabelsMaxRectStroke, paintLinear, paintLines1, paintLinearMin, paintLinearMax;
    private int n = 3;
    private float [] THIRD_OCTAVE = {16, 20, 25, 31.5f, 40, 50, 63, 80, 100, 125, 160, 200, 250, 315, 400, 500,
                                     630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 5000, 6300, 8000, 10000, 12500, 16000, 20000};
    String [] THIRD_OCTAVE_LABEL = {"16", "20", "25", "31.5", "40", "50", "63", "80", "100", "125", "160", "200", "250", "315", "400", "500",
                "630", "800", "1000", "1250", "1600", "2000", "2500", "3150", "4000", "5000", "6300", "8000", "10000", "12500", "16000", "20000"};
    private float[] dbBand;
    private float[] dbBandMin = new float[THIRD_OCTAVE.length];
    private float[] dbBandMax = new float[THIRD_OCTAVE.length];

    private float fontSize;

    private float test, test0, test1, test2;

    public PlotThirdOctave(Context context) {
        this(context, null, 0);
    }

    public PlotThirdOctave(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlotThirdOctave(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paintLines1 = new Paint();
        paintLines1.setColor(0xffcccccc);
        paintLines1.setStyle(Paint.Style.FILL_AND_STROKE);
        paintLines1.setStrokeWidth(1.0f);

        paintLines2 = new Paint();
        paintLines2.setColor(0xff000000);
        paintLines2.setStyle(Paint.Style.FILL_AND_STROKE);
        paintLines2.setStrokeWidth(1.0f);


        paintLabelsY = new Paint();
        paintLabelsY.setColor(Color.BLACK);
        paintLabelsY.setTextSize(20);
        paintLabelsY.setTextAlign(Paint.Align.RIGHT);

        paintLabelsX = new Paint();
        paintLabelsX.setColor(Color.BLACK);
        paintLabelsX.setTextSize(20);
        paintLabelsX.setTextAlign(Paint.Align.CENTER);

        paintLabelsMax = new Paint();
        paintLabelsMax.setColor(0xffc60000);
        paintLabelsMax.setTextSize(20);
        paintLabelsMax.setTextAlign(Paint.Align.RIGHT);

        paintLabelsMaxRectFill = new Paint();
        paintLabelsMaxRectFill.setColor(getResources().getColor(R.color.background));
        paintLabelsMaxRectFill.setStyle(Paint.Style.FILL);

        paintLabelsMaxRectStroke = new Paint();
        paintLabelsMaxRectStroke.setColor(Color.BLACK);
        paintLabelsMaxRectStroke.setStyle(Paint.Style.STROKE);
        paintLabelsMaxRectStroke.setStrokeWidth(2.0f);

        paintLinearMax = new Paint();
        paintLinearMax.setColor(Color.BLUE);
        paintLinearMax.setStyle(Paint.Style.FILL_AND_STROKE);
        paintLinearMax.setStrokeWidth(2.0f);

        paintLinear = new Paint();
        paintLinear.setColor(0xffc60000);
        paintLinear.setStyle(Paint.Style.FILL_AND_STROKE);
        paintLinear.setStrokeWidth(2.0f);

        paintLinearMin = new Paint();
        paintLinearMin.setColor(Color.GREEN);
        paintLinearMin.setStyle(Paint.Style.FILL_AND_STROKE);
        paintLinearMin.setStrokeWidth(2.0f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();

        fontSize = w * 0.04f;
        paintLabelsX.setTextSize(fontSize);
        paintLabelsY.setTextSize(fontSize);
        paintLabelsMax.setTextSize(fontSize);

        float h = getHeight();
        float w_plot = getWidth() - paintLabelsX.measureText(Float.toString(paintLabelsY.getTextSize())); //* 0.925f;
        float deltaLabelX = (paintLabelsX.descent() - paintLabelsX.ascent());
        float deltaLabelY = (paintLabelsY.descent() - paintLabelsY.ascent());
        float deltaLabelMax = (paintLabelsMax.descent() - paintLabelsMax.ascent());
        float h_plot = getHeight() - deltaLabelX - deltaLabelY;
        float yMaxAxis = 110f;
        float barWeight = w_plot / (float) THIRD_OCTAVE.length;

        // grafico FFT non pesato
        String [] vertical_lines_label = {"16", "", "", "31.5", "", "", "63", "", "", "125", "", "", "250", "", "", "500",
                "", "", "1k", "", "", "2k", "", "", "4k", "", "", "8k", "", "", "16k", ""};




        if (dbBand != null) {
            float dbyMaxIst = dbBand[0];
            int iMaxIst = 0;

            for (int i = 0; i < dbBand.length; i++) {
                float x_ini = w - w_plot + i * barWeight;

                float yMax = dbBandMax[i] * h_plot / yMaxAxis;
                yMax = h_plot - yMax;
                canvas.drawRect(x_ini, deltaLabelY + yMax, x_ini + barWeight, deltaLabelY + h_plot, paintLinearMax);

                float y = dbBand[i] * h_plot / yMaxAxis;
                y = h_plot - y;
                canvas.drawRect(x_ini, deltaLabelY + y, x_ini + barWeight, deltaLabelY + h_plot, paintLinear);

                if(dbyMaxIst < dbBand[i]){
                    //yMaxIst = y;
                    dbyMaxIst = dbBand[i];
                    //xMaxIst = x_ini + barWeight/2;
                    iMaxIst = i;
                }

                float yMin = dbBandMin[i] * h_plot / yMaxAxis;
                yMin = h_plot - yMin;
                canvas.drawRect(x_ini, deltaLabelY + yMin, x_ini + barWeight, deltaLabelY + h_plot, paintLinearMin);

                canvas.drawLine(x_ini + barWeight / 2, deltaLabelY + h_plot, x_ini + barWeight / 2, deltaLabelY + h_plot + paintLabelsX.ascent() / 3, paintLines2);
                if (vertical_lines_label[i] != "") {
                    canvas.drawText("" + vertical_lines_label[i], x_ini + barWeight / 2, deltaLabelY + h_plot - paintLabelsX.ascent(), paintLabelsX);
                    canvas.drawLine(x_ini + barWeight / 2, deltaLabelY + h_plot, x_ini + barWeight / 2, deltaLabelY + h_plot + paintLabelsX.ascent()*0.75f, paintLines2);
                }
            }
            // Linea verticale 0
            canvas.drawLine(w - w_plot, deltaLabelY + h_plot - yMaxAxis * h_plot / yMaxAxis, w - w_plot, deltaLabelY + h_plot, paintLines2);

            // Linee orizzontali
            for (int i = 5; i <= yMaxAxis; i += 10) {
                canvas.drawLine(w - w_plot, deltaLabelY + h_plot - i * h_plot / yMaxAxis, w, deltaLabelY + h_plot - i * h_plot / yMaxAxis, paintLines1);
            }
            for (int i = 0; i <= yMaxAxis; i += 10) {
                canvas.drawLine(w - w_plot, deltaLabelY + h_plot - i * h_plot / yMaxAxis, w, deltaLabelY + h_plot - i * h_plot / yMaxAxis, paintLines2);
                canvas.drawText("" + i, w - w_plot - 5, deltaLabelY + h_plot - i * h_plot / yMaxAxis + paintLabelsX.descent(), paintLabelsY);
            }

            // scrivi massimo
            canvas.drawRect(w - paintLabelsMax.measureText(" 20000 Hz ") - 10, deltaLabelMax, w - 10, 3 * deltaLabelMax + paintLabelsMax.descent(), paintLabelsMaxRectStroke);
            canvas.drawRect(w - paintLabelsMax.measureText(" 20000 Hz ") - 10, deltaLabelMax, w - 10, 3 * deltaLabelMax  + paintLabelsMax.descent(), paintLabelsMaxRectFill);
            canvas.drawText(String.format("%.1f", dbyMaxIst) + " dB ", w - 10, 2 * deltaLabelMax, paintLabelsMax);
            canvas.drawText(String.format("%.0f", THIRD_OCTAVE[iMaxIst]) + " Hz ", w - 10, 3 * deltaLabelMax, paintLabelsMax);
        }

        // test
//        canvas.drawText("" + dbBandMin[0] + "  " + dbBandMin[10] + "  " + dbBandMin[20] + "  " + dbBandMin[dbBandMin.length - 1], w / 2, h / 2, paintLabelsX);
    }

    public void setDataPlot(float[] data1, float[] data2, float[] data3) {

        if (dbBand == null || dbBand.length != data1.length)
            dbBand = new float[data1.length];
        System.arraycopy(data1, 0, dbBand, 0, data1.length);

        if (dbBandMin == null || dbBandMin.length != data2.length)
            dbBandMin = new float[data2.length];
        System.arraycopy(data2, 0, dbBandMin, 0, data2.length);

        if (dbBandMax == null || dbBandMax.length != data3.length)
            dbBandMax = new float[data3.length];
        System.arraycopy(data3, 0, dbBandMax, 0, data3.length);

        // parte per bande senza valori
        dbBand[1] = dbBand[0];
        dbBand[3] = dbBand[2];
        dbBand[4] = dbBand[5];
        dbBand[6] = dbBand[7];

        dbBandMin[1] = dbBandMin[0];
        dbBandMin[3] = dbBandMin[2];
        dbBandMin[4] = dbBandMin[5];
        dbBandMin[6] = dbBandMin[7];

        dbBandMax[1] = dbBandMax[0];
        dbBandMax[3] = dbBandMax[2];
        dbBandMax[4] = dbBandMax[5];
        dbBandMax[6] = dbBandMax[7];

        invalidate();
    }
}