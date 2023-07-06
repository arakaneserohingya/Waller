package com.blogger.waller.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.blogger.waller.R;
import com.blogger.waller.databinding.ActivityFullScreenImageBinding;
import com.blogger.waller.utils.Tools;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

public class ActivityFullScreenImage extends AppCompatActivity {

    public static final String EXTRA_IMG = "EXTRA_IMG";
    public static final String EXTRA_POS = "EXTRA_POS";

    public static void navigate(Activity activity, ArrayList<String> images, int position, View sharedView) {
        Intent intent = new Intent(activity, ActivityFullScreenImage.class);
        intent.putExtra(EXTRA_IMG, images);
        intent.putExtra(EXTRA_POS, position);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedView, EXTRA_IMG);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    public static void navigate(Activity activity, String image) {
        Intent intent = new Intent(activity, ActivityFullScreenImage.class);
        ArrayList<String> images = new ArrayList<>();
        images.add(image);
        intent.putExtra(EXTRA_IMG, images);
        intent.putExtra(EXTRA_POS, 0);
        activity.startActivity(intent);
    }

    private AdapterFullScreenImage adapter;
    private ActivityFullScreenImageBinding binding;

    private ArrayList<String> items = new ArrayList<>();
    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullScreenImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // animation transition
        ViewCompat.setTransitionName(binding.pager, EXTRA_IMG);

        items = (ArrayList<String>) getIntent().getSerializableExtra(EXTRA_IMG);
        position = getIntent().getIntExtra(EXTRA_POS, 0);

        initComponent();

        Tools.RTLMode(getWindow());
        Tools.darkNavigation(this);
    }

    private void initComponent() {
        adapter = new AdapterFullScreenImage(this, items);
        final int total = adapter.getCount();
        binding.pager.setAdapter(adapter);
        binding.textPage.setText(String.format(getString(R.string.image_of), 1, total));
        if (items.size() == 1) binding.textPage.setVisibility(View.GONE);

        // displaying selected image first
        binding.pager.setCurrentItem(position);
        binding.pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int pos, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int pos) {
                binding.textPage.setText(String.format(getString(R.string.image_of), (pos + 1), total));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        findViewById(R.id.btn_close).setOnClickListener(view -> finish());
    }

    private class AdapterFullScreenImage extends PagerAdapter {

        private Activity act;
        private List<String> image_paths;
        private LayoutInflater inflater;

        // constructor
        public AdapterFullScreenImage(Activity activity, List<String> imagePaths) {
            this.act = activity;
            this.image_paths = imagePaths;
        }

        @Override
        public int getCount() {
            return this.image_paths.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PhotoView image;
            inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewLayout = inflater.inflate(R.layout.item_slider_image, container, false);

            image = viewLayout.findViewById(R.id.image);
            Tools.displayImage(act, image, image_paths.get(position));
            (container).addView(viewLayout);

            return viewLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            (container).removeView((RelativeLayout) object);

        }

    }

}