package it.piemonte.arpa.openoise;

/**
 * Created by stefmase on 16/04/2015.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by eugeniosoddu on 27/03/15.
 */
public class PlotFFT extends View {

    private Paint paintLines1, paintLabelsY, paintLabelsX, paintLabelsMax,paintLabelsMaxRectFill,paintLabelsMaxRectStroke,paintLinear, paintWeight, paintLines2;
    //private float db1, db2;
    private Path path;
    private float[] inData1, inData2;
    private int BAND_NUMBER;
    private double BAND_WIDTH;

    private float fontSize;

    public PlotFFT(Context context) {
        this(context, null, 0);
    }

    public PlotFFT(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlotFFT(Context context, AttributeSet attrs, int defStyleAttr) {
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

        paintLinear = new Paint();
        paintLinear.setColor(Color.BLUE);
        paintLinear.setStyle(Paint.Style.STROKE);
        paintLinear.setStrokeWidth(2.0f);

        paintWeight = new Paint();
        paintWeight.setColor(0xffc60000);
        paintWeight.setStyle(Paint.Style.STROKE);
        paintWeight.setStrokeWidth(2.0f);

        paintLabelsMax = new Paint();
        paintLabelsMax.setTextSize(20);
        paintLabelsMax.setTextAlign(Paint.Align.RIGHT);

        paintLabelsMaxRectFill = new Paint();
        paintLabelsMaxRectFill.setColor(getResources().getColor(R.color.background));
        paintLabelsMaxRectFill.setStyle(Paint.Style.FILL);

        paintLabelsMaxRectStroke = new Paint();
        paintLabelsMaxRectStroke.setColor(Color.BLACK);
        paintLabelsMaxRectStroke.setStyle(Paint.Style.STROKE);
        paintLabelsMaxRectStroke.setStrokeWidth(2.0f);

        path = new Path();

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
        float w_plot = getWidth() - paintLabelsY.measureText(Float.toString(paintLabelsY.getTextSize()));
        float deltaLabelX = (paintLabelsX.descent() - paintLabelsX.ascent());
        float deltaLabelY = (paintLabelsY.descent() - paintLabelsY.ascent());
        float deltaLabelMax = (paintLabelsMax.descent() - paintLabelsMax.ascent());
        float h_plot = getHeight() - deltaLabelX - deltaLabelY;
        float yMax = 110f;
        float dbyMaxIst = 0;
        float dbyMaxIstA = 0;
        int iMaxIst = 0;
        int iMaxIstA = 0;

        // grafico FFT non pesato
        if (inData1 != null) {

            path.rewind();
            path.moveTo(w - w_plot, deltaLabelY + h_plot);
            for (int i = 0; i < inData1.length; i++) {
                float x =  w - w_plot + w_plot
                        * (float) (Math.log(i + 1.0) / Math.log(inData1.length));
                float y = inData1[i] * h_plot / yMax;
                if (Float.isInfinite(y) || Float.isNaN(y))
                    y = 0;
                y = deltaLabelY + h_plot - y;

                if(dbyMaxIst < inData1[i]){
                    dbyMaxIst = inData1[i];
                    iMaxIst = i;
                }

                path.lineTo(x, y);
            }
            canvas.drawPath(path, paintLinear);
        }

        // grafico FFT pesato
        if (inData2 != null) {

            path.rewind();
            path.moveTo(w - w_plot, deltaLabelY + h_plot);

            for (int i = 0; i < inData2.length; i++) {
                float x = w - w_plot + w_plot
                        * (float) (Math.log(i + 1.0) / Math.log(inData2.length));
                float y = inData2[i] * h_plot / yMax;
                if (Float.isInfinite(y) || Float.isNaN(y))
                    y = 0;
                y = deltaLabelY + h_plot - y;

                if(dbyMaxIstA < inData2[i]){
                    dbyMaxIstA = inData2[i];
                    iMaxIstA = i;
                }

                path.lineTo(x, y);
            }
            canvas.drawPath(path, paintWeight);

            // Linee orizzontali
            for (int i = 5; i <= yMax; i += 10) {
                canvas.drawLine(w - w_plot, deltaLabelY + h_plot - i * h_plot / yMax, w, deltaLabelY + h_plot - i * h_plot / yMax, paintLines1);
            }
            for (int i = 0; i <= yMax; i += 10) {
                canvas.drawLine(w - w_plot, deltaLabelY + h_plot - i * h_plot / yMax, w, deltaLabelY + h_plot - i * h_plot / yMax, paintLines2);
                canvas.drawText("" + i, w - w_plot - 5, deltaLabelY + h_plot - i * h_plot / yMax + paintLabelsX.descent(), paintLabelsY);
            }

            // Linee verticali
            int [] vertical_lines1 = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 200, 300, 400, 500, 600, 700, 800, 900, 
                    1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000, 20000};

            for (int line = 0; line < vertical_lines1.length; line++) {

                float i = vertical_lines1[line] /  (float) (BAND_WIDTH);

                float x = w - w_plot + w_plot * (float) (Math.log(i + 1) / Math.log(inData2.length));
                canvas.drawLine(x, deltaLabelY + 0, x, deltaLabelY + h_plot, paintLines1);
            }
            
            int [] vertical_lines2 = {0, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000};
            String [] vertical_lines_label = {"0", "20", "50", "100", "200", "500", "1k", "2k","5k", "10k", "20k"};

            for (int line = 0; line < vertical_lines2.length; line++) {

                float i = vertical_lines2[line] /  (float) (BAND_WIDTH);

                float x = w - w_plot + w_plot * (float) (Math.log(i + 1) / Math.log(inData2.length));
                canvas.drawText("" + vertical_lines_label[line], x, deltaLabelY + h_plot - paintLabelsX.ascent(), paintLabelsX);
                canvas.drawLine(x, deltaLabelY + 0, x, deltaLabelY + h_plot, paintLines2);
            }
            //canvas.drawText("" + paintLabelsX.descent(), w/2, h/2, paintLabelsX);
        }

        // scrivi massimo
        canvas.drawRect(w - paintLabelsMax.measureText("    20000 Hz ") - 10, deltaLabelMax, w - 10, 5 * deltaLabelMax + paintLabelsMax.descent(), paintLabelsMaxRectStroke);
        canvas.drawRect(w - paintLabelsMax.measureText("    20000 Hz ") - 10, deltaLabelMax, w - 10, 5 * deltaLabelMax  + paintLabelsMax.descent(), paintLabelsMaxRectFill);
        paintLabelsMax.setColor(Color.BLUE);
        canvas.drawText(String.format("%.1f", dbyMaxIst) + " dB ", w - 10, 2 * deltaLabelMax, paintLabelsMax);
        canvas.drawText(String.format("%.0f", iMaxIst*BAND_WIDTH) + " Hz ", w - 10, 3 * deltaLabelMax, paintLabelsMax);
        paintLabelsMax.setColor(0xffc60000);
        canvas.drawText(String.format("%.1f", dbyMaxIstA) + " dB(A) ", w - 10, 4 * deltaLabelMax, paintLabelsMax);
        canvas.drawText(String.format("%.0f", iMaxIstA*BAND_WIDTH) + " Hz ", w - 10, 5 * deltaLabelMax, paintLabelsMax);

    }

    public void setDataPlot(int BLOCK_SIZE_FFT, double BAND_WIDTH, float[] data1, float[] data2) {

        this.BAND_NUMBER = BLOCK_SIZE_FFT / 2;
        this.BAND_WIDTH = BAND_WIDTH;
        //this.db1 = (float) Math.floor(db1 * 10) / 10;
        //this.db2 = (float) Math.floor(db2 * 10) / 10;
        if (inData1 == null || inData1.length != data1.length)
            inData1 = new float[data1.length];
        System.arraycopy(data1, 0, inData1, 0, data1.length);
        if (inData2 == null || inData2.length != data2.length)
            inData2 = new float[data2.length];
        System.arraycopy(data2, 0, inData2, 0, data2.length);
        invalidate();
    }
}