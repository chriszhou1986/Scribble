package com.github.alexrichards.scribble;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.github.alexrichards.scribble.widget.BufferBuilder;
import com.github.alexrichards.scribble.widget.ColorView;
import com.github.alexrichards.scribble.widget.PaintBrush;
import com.github.alexrichards.scribble.widget.ScribbleCanvas;
import com.github.alexrichards.scribble.widget.SizeView;

public class ExampleCanvasActivity extends ActionBarActivity {

    private static final int BUFFER_SIZE = 500;

    private static final int[] COLORS = {
            0xFF000000,
            0xFFFFFFFF,
            0xFFFF0000,
            0xFFFFFF00,
            0xFF00FF00,
            0xFF00FFFF,
            0xFF0000FF,
            0xFFFF00FF,
    };

    private static final float[] SIZES = {
            0,
            5.0f,
            10.0f,
    };

    private DrawerLayout drawerLayout;
    private ScribbleCanvas scribbleCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_examplecanvas);

        drawerLayout = (DrawerLayout) findViewById(R.id.view_container);

        scribbleCanvas = (ScribbleCanvas) findViewById(R.id.view_canvas);
        scribbleCanvas.setBuffer(new BufferBuilder(BUFFER_SIZE, BUFFER_SIZE).background(0xFFFFFFFF).build());

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(0x00000000);

        scribbleCanvas.setBrush(new PaintBrush(paint));

        final PaletteAdapter.PaletteCallback paletteCallback = new PaletteAdapter.PaletteCallback() {
            @Override
            public void onColorSelected(int color) {
                final PaintBrush brush = (PaintBrush) scribbleCanvas.getBrush();
                final Paint paint = brush.getPaint();
                paint.setColor(color);
                scribbleCanvas.setBrush(new PaintBrush(paint));
                drawerLayout.closeDrawer(Gravity.END);
            }

            @Override
            public void onSizeSelected(float size) {
                final PaintBrush brush = (PaintBrush) scribbleCanvas.getBrush();
                final Paint paint = brush.getPaint();
                paint.setStrokeWidth(size);
                scribbleCanvas.setBrush(new PaintBrush(paint));
                drawerLayout.closeDrawer(Gravity.END);
            }
        };

        final PaletteAdapter paletteAdapter = new PaletteAdapter(paletteCallback, COLORS, SIZES);

        final GridView gridView = (GridView) findViewById(R.id.view_palette);
        gridView.setAdapter(paletteAdapter);
        gridView.setOnItemClickListener(paletteAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.activity_examplecanvas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_palette) {
            if (drawerLayout.isDrawerOpen(Gravity.END)) {
                drawerLayout.closeDrawer(Gravity.END);
            } else {
                drawerLayout.openDrawer(Gravity.END);
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private static class PaletteAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

        private static final int TYPE_COLOR = 0;
        private static final int TYPE_SIZE = 1;
        private static final int TYPE_EMPTY = 2;
        private static final int TYPE_COUNT = 3;

        private final PaletteCallback paletteCallback;

        private final int[] colors;
        private final float[] sizes;

        public PaletteAdapter(PaletteCallback paletteCallback, int[] colors, float[] sizes) {
            this.paletteCallback = paletteCallback;
            this.colors = colors;
            this.sizes = sizes;
        }

        @Override
        public int getCount() {
            return Math.max(colors.length, sizes.length) * 2;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            final int t = position % 2;
            final int p = position / 2;
            if (t == TYPE_COLOR && p < colors.length) {
                return TYPE_COLOR;
            } else if (t == TYPE_SIZE && p < sizes.length) {
                return TYPE_SIZE;
            }
            return TYPE_EMPTY;
        }

        @Override
        public int getViewTypeCount() {
            return TYPE_COUNT;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            switch (getItemViewType(position)) {
                case TYPE_COLOR:
                    return getColorView(position / 2, convertView, parent);
                case TYPE_SIZE:
                    return getSizeView(position / 2, convertView, parent);
                case TYPE_EMPTY:
                    return getEmptyView(convertView, parent);
                default:
                    throw new RuntimeException("Oh dear!!");
            }
        }

        private View getColorView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_color, parent, false);
            }

            final ColorView colorView = (ColorView) convertView.findViewById(R.id.view_color);
            colorView.setColor(colors[position]);

            return convertView;
        }

        private View getSizeView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_size, parent, false);
            }

            final SizeView sizeView = (SizeView) convertView.findViewById(R.id.view_size);
            sizeView.setSize(sizes[sizes.length - 1], sizes[position]);

            return convertView;
        }

        private View getEmptyView(View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_empty, parent, false);
            }
            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) != TYPE_EMPTY;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (paletteCallback != null) {
                final int type = getItemViewType(position);
                if (type == TYPE_COLOR) {
                    paletteCallback.onColorSelected(colors[position / 2]);
                } else if (type == TYPE_SIZE) {
                    paletteCallback.onSizeSelected(sizes[position / 2]);
                }
            }
        }

        public interface PaletteCallback {

            void onColorSelected(int color);

            void onSizeSelected(float size);

        }
    }
}