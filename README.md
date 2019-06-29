# DragPhoto
仿探探可拖拽图片功能


<img width="400" src="https://img-blog.csdnimg.cn/20190629190826175.gif"/>

## first
add repository
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

add dependency
```
dependencies {
        implementation 'com.github.SineyCoder:DragPhoto:1.0'
}
```

## use

如果想要在DragFrameLayout中继续放子View，可直接添加
```
<com.siney.lib.dragphoto.DragFrameLayout
        android:id="@+id/drag_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:gap="5dp"
        app:placeholder="@android:drawable/ic_input_add">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="后续布局"/>

    </com.siney.lib.dragphoto.DragFrameLayout>
```
```
DragFrameLayout layout = findViewById(R.id.drag_layout);
layout.setListener(this);//添加监听，有4个不同的回调，
分别是1.placeholder图片加载完成 2.图片点击事件 3.图片是否可拖拽（return false表示不可拖拽） 4.图片是否允许交换（return false表示不可交换）
```

## 注意

```
//里面的每一个图片都是DragImage，里面有num以及path，在设置图片时请使用下列进行设置，内部使用Glide进行图片渲染
image.setImage(path, type);
```

图片如果看不完整可以访问我的博客
https://blog.csdn.net/a568283992/article/details/94192662
