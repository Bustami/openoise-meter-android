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

public class PlotSLMHistory extends View {

    private Paint paintLines1, paintLines2, paintLabelsY, paintWeight;
    private Path path;
    private float[] dbHistory = new float[60];

    private float fontSize;

    public PlotSLMHistory(Context context) {
        this(context, null, 0);
    }

    public PlotSLMHistory(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlotSLMHistory(Context context, AttributeSet attrs, int defStyleAttr) {
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

        paintWeight = new Paint();
        paintWeight.setColor(0xffc60000);
        paintWeight.setStyle(Paint.Style.FILL_AND_STROKE);
        paintWeight.setStrokeWidth(2.0f);
        paintWeight.setTextSize(130);
        paintWeight.setTextAlign(Paint.Align.RIGHT);

        path = new Path();

        for (int i=0; i<60; i++)
        {
            dbHistory[i] = 0;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();

        fontSize = w * 0.04f;
        paintLabelsY.setTextSize(fontSize);

        float h = getHeight();
        float deltaLabelY = (paintLabelsY.descent() - paintLabelsY.ascent());
        float w_plot = getWidth() - paintLabelsY.measureText(Float.toString(paintLabelsY.getTextSize()));
        float h_plot = getHeight() - deltaLabelY - paintLabelsY.descent();
        float yMaxAxis = 110f;
        float barWeight = w_plot / 60f;
        float x_ini = w - w_plot;


        if (dbHistory != null) {
            path.rewind();
            path.moveTo(w - w_plot, h_plot);

            for (int i = 0; i < 60; i++) {
                float y = dbHistory[i] * h_plot / yMaxAxis;
                if (Float.isInfinite(y) || Float.isNaN(y))
                    y = 0;
                canvas.drawRect(x_ini, deltaLabelY + h_plot - y, x_ini + barWeight, deltaLabelY + h_plot, paintWeight);
                x_ini = x_ini + barWeight;
            }
            canvas.drawPath(path, paintWeight);
        }

        // test
//        canvas.drawText("" + x_ini + " " +  (w - w_plot) + " " +  w  + " " +  x_ini + barWeight, w, h_plot/2, paintLabelsY);

        // Linea verticale 0 e fine plot
        canvas.drawLine(w - w_plot, deltaLabelY, w - w_plot, deltaLabelY + h_plot, paintLines2);
//        canvas.drawLine(w, deltaLabelY, w, deltaLabelY + h_plot, paintLines2);
        
        // Linee orizzontali
        for (int i = 5; i <= yMaxAxis; i += 10) {
            canvas.drawLine(w - w_plot, deltaLabelY + h_plot - i * h_plot / yMaxAxis, w, deltaLabelY + h_plot - i * h_plot / yMaxAxis, paintLines1);
        }
        for (int i = 0; i <= yMaxAxis; i += 10) {
            canvas.drawLine(w - w_plot, deltaLabelY + h_plot - i * h_plot / yMaxAxis, w, deltaLabelY + h_plot - i * h_plot / yMaxAxis, paintLines2);
            canvas.drawText("" + i, w - w_plot - 5, deltaLabelY + h_plot - i * h_plot / yMaxAxis + paintLabelsY.descent(), paintLabelsY);
        }


    }

    public void setDataPlot(float[] data1) {

        if (dbHistory == null || dbHistory.length != data1.length)
            dbHistory = new float[data1.length];
        System.arraycopy(data1, 0, dbHistory, 0, data1.length);

        invalidate();
    }
}