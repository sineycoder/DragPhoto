package com.siney.dragphoto;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.siney.lib.dragphoto.DragFrameLayout;
import com.siney.lib.dragphoto.DragImageView;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.util.List;

public class MainActivity extends AppCompatActivity implements DragFrameLayout.OnDragLayoutListener {

    private DragFrameLayout layout;

    private DragImageView curImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean b = PermissionUtils.checkAllPermission(this);
        if(b){
            initView();
            initListener();
        }else{
            Toast.makeText(this, "请开启读取SD卡权限", Toast.LENGTH_LONG).show();
        }
    }

    private void initListener() {
        layout.setListener(this);
    }

    private void initView() {
        layout = findViewById(R.id.drag_layout);
    }

    @Override
    public void onFinish(List<DragImageView> images) {
        Log.e("Drag", "onFinish");
    }

    @Override
    public void onImageClick(DragImageView image) {
        Log.e("Drag", "onImageClick");
        curImage = image;
        Matisse.from(this)
                .choose(MimeType.ofImage(), false)//选择mime类型
                .maxSelectable(1)//最多选择图片数量
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f) // 缩略图的比例
                .imageEngine(new GlideEngine()) // 使用的图片加载引擎
                .forResult(1); // 设置作为标记的请求码
    }

    @Override
    public boolean allowDrag(DragImageView image) {
        Log.e("Drag", "allowDrag");
        return true;
    }

    @Override
    public boolean allowExchange(DragImageView dragImage, DragImageView exchangeImage) {
        Log.e("Drag", "allowExchange");
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 1 && resultCode == RESULT_OK){
            String path = Matisse.obtainPathResult(data).get(0);
            curImage.setImage(path, DragImageView.USE);
        }
    }
}
