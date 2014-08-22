package company.memo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class CameraOverlay extends SurfaceView implements SurfaceHolder.Callback {

    private DrawThread drawThread;

    public CameraOverlay(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas;
        canvas = holder.lockCanvas(null);
        if (canvas != null) {
            Paint p = new Paint();
            p.setARGB(100, 255, 255, 255);
            canvas.drawCircle(500, 500, 200, p);
        }
        holder.unlockCanvasAndPost(canvas);


        drawThread = new DrawThread(getHolder());
        drawThread.setRunning(true);
        drawThread.start();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;

        drawThread.setRunning(false);
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }


    class DrawThread extends Thread {

        private boolean running = false;
        private SurfaceHolder surfaceHolder;

        public DrawThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }
        @Override
        public void run() {
            Canvas canvas;
            while (running) {
                canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas(null);
                    if (canvas == null)
                        continue;
                    Paint p = new Paint();
                    Random rand = new Random();

                    p.setARGB(200, rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
                    canvas.drawCircle(500, 500, 200, p);
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}
