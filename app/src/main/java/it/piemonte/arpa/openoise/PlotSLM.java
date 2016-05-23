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

public class PlotSLM extends View {

    private Paint paintLines1, paintLines2, paintLabelsY, paintdb,paintdbMin,paintdbMax;
    private float db,dbMin,dbMax;
    private Path path;
    private float[] inData1, inData2;
    private float fontSize;

    public PlotSLM(Context context) {
        this(context, null, 0);
    }

    public PlotSLM(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlotSLM(Context context, AttributeSet attrs, int defStyleAttr) {
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

        paintdbMax = new Paint();
        paintdbMax.setColor(Color.BLUE);
        paintdbMax.setStyle(Paint.Style.FILL_AND_STROKE);
        paintdbMax.setStrokeWidth(2.0f);
        paintdbMax.setTextAlign(Paint.Align.RIGHT);

        paintdb = new Paint();
        paintdb.setColor(0xffc60000);
        paintdb.setStyle(Paint.Style.FILL_AND_STROKE);
        paintdb.setStrokeWidth(2.0f);
        paintdb.setTextSize(80);
        paintdb.setTextAlign(Paint.Align.RIGHT);

        paintdbMin = new Paint();
        paintdbMin.setColor(Color.GREEN);
        paintdbMin.setStyle(Paint.Style.FILL_AND_STROKE);
        paintdbMin.setStrokeWidth(2.0f);
        paintdbMin.setTextSize(80);
        paintdbMin.setTextAlign(Paint.Align.RIGHT);



        path = new Path();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();

        fontSize = w * 0.04f;
        paintLabelsY.setTextSize(fontSize);

        float deltaLabelY = (paintLabelsY.descent() - paintLabelsY.ascent());
        float h_plot = getHeight() - deltaLabelY - paintLabelsY.descent();
        float yMaxAxis = 110f;
        float barWeight = w * 0.33f;



        // LABEL MAX MEDIA E MIN - TOLTA
/*        paintdbMax.setTextSize(20);
        float yLabel = deltaLabelY;
        canvas.drawText("Max", w * 0.95f, deltaLabelY, paintdbMax);
        paintdbMax.setTextSize(50);
        yLabel -= paintdb.ascent();
        canvas.drawText("" + dbMax, w * 0.95f, yLabel, paintdbMax);

        paintdb.setTextSize(20);
        yLabel -= 2*paintdb.ascent();
        canvas.drawText("Ist", w * 0.95f, yLabel, paintdb);
        paintdb.setTextSize(50);
        yLabel -= paintdb.ascent();
        canvas.drawText("" + db, w * 0.95f, yLabel, paintdb);

        paintdbMin.setTextSize(20);
        yLabel -= 2*paintdbMin.ascent();
        canvas.drawText("Min", w * 0.95f, yLabel, paintdbMin);
        paintdbMin.setTextSize(50);
        yLabel -= paintdbMin.ascent();
        canvas.drawText("" + dbMin, w * 0.95f, yLabel, paintdbMin);*/


//        canvas.drawText("" + dbMax, w * 0.95f, deltaLabelY + h_plot/2 + paintdbMax.descent() - (paintdbMax.descent() - paintdbMax.ascent()), paintdbMax);
//        canvas.drawText("" + db, w * 0.95f, deltaLabelY + h_plot/2 + paintdbMax.descent(), paintdb);
//        canvas.drawText("" + dbMin, w * 0.95f, deltaLabelY + h_plot/2 + paintdbMax.descent() + (paintdbMin.descent() - paintdbMin.ascent()), paintdbMin);

        canvas.drawRect(w * 0.33f, deltaLabelY + h_plot - dbMax * h_plot / yMaxAxis, w * 0.33f + barWeight, deltaLabelY + h_plot, paintdbMax);
        canvas.drawRect(w *0.33f, deltaLabelY + h_plot - db * h_plot / yMaxAxis, w *0.33f + barWeight, deltaLabelY + h_plot, paintdb);
        canvas.drawRect(w *0.33f, deltaLabelY + h_plot - dbMin * h_plot / yMaxAxis, w *0.33f + barWeight, deltaLabelY + h_plot, paintdbMin);

        // Linea verticale 0 e fine barra
        canvas.drawLine(w * 0.33f, deltaLabelY, w *0.33f, deltaLabelY + h_plot, paintLines2);
        canvas.drawLine(w *0.33f + barWeight, deltaLabelY, w *0.33f + barWeight, deltaLabelY + h_plot, paintLines2);

        // Linee orizzontali
        for (int i = 5; i <= yMaxAxis; i += 10) {
            canvas.drawLine(w *0.33f, deltaLabelY + h_plot - i * h_plot / yMaxAxis, w *0.33f + barWeight, deltaLabelY + h_plot - i * h_plot / yMaxAxis, paintLines1);
        }
        for (int i = 0; i <= yMaxAxis; i += 10) {
            canvas.drawLine(w *0.33f, deltaLabelY + h_plot - i * h_plot / yMaxAxis, w *0.33f + barWeight, deltaLabelY + h_plot - i * h_plot / yMaxAxis, paintLines2);
            canvas.drawText("" + i, w *0.32f, deltaLabelY + h_plot - i * h_plot / yMaxAxis + paintLabelsY.descent(), paintLabelsY);
        }

    }

    public void setDataPlot(float db, float dbMin, float dbMax) {
        this.db = (float) Math.floor(db * 10) / 10;
        this.dbMin = (float) Math.floor(dbMin * 10) / 10;
        this.dbMax = (float) Math.floor(dbMax * 10) / 10;
        invalidate();
    }
}