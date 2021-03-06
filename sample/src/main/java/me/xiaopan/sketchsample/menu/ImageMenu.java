/*
 * Copyright (C) 2016 Peng fei Pan <sky@xiaopan.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.sketchsample.menu;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.text.format.Formatter;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import me.xiaopan.sketch.Sketch;
import me.xiaopan.sketch.SketchImageView;
import me.xiaopan.sketch.cache.DiskCache;
import me.xiaopan.sketch.drawable.LoadingDrawable;
import me.xiaopan.sketch.drawable.SketchDrawable;
import me.xiaopan.sketch.feature.large.LargeImageViewer;
import me.xiaopan.sketch.feature.zoom.ImageZoomer;
import me.xiaopan.sketch.request.UriScheme;
import me.xiaopan.sketch.util.SketchUtils;

public class ImageMenu {

    private Activity activity;
    private SketchImageView imageView;

    private AlertDialog tempAlertDialog;

    public ImageMenu(Activity activity, SketchImageView imageView) {
        this.activity = activity;
        this.imageView = imageView;
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("菜单");

        final boolean supportZoom = imageView.isSupportZoom();
        final ImageZoomer imageZoomer = supportZoom ? imageView.getImageZoomer() : null;
        final boolean supportLargeImage = imageView.isSupportLargeImage();
        final LargeImageViewer largeImageViewer = imageView.getLargeImageViewer();

        String[] items = new String[5];
        items[0] = "查看详细信息";
        items[1] = "修改ScaleType（" + (supportZoom ? imageZoomer.getScaleType() : imageView.getScaleType()) + "）";
        items[2] = supportLargeImage ? (largeImageViewer.isShowTileRect() ? "不显示分块区域" : "显示分块区域") : "分块区域（未开启大图功能）";
        items[3] = supportZoom ? (imageZoomer.isReadMode() ? "关闭阅读模式" : "开启阅读模式") : ("阅读模式（未开启缩放功能）");
        items[4] = supportZoom ? ("顺时针旋转90度（" + imageZoomer.getRotateDegrees() + "）") : "旋转图片（未开启缩放功能）";
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tempAlertDialog.dismiss();

                switch (which) {
                    case 0:
                        showDetailInfo();
                        break;
                    case 1:
                        showScaleTypeMenu();
                        break;
                    case 2:
                        if (supportLargeImage) {
                            boolean newShowTileRect = !largeImageViewer.isShowTileRect();
                            largeImageViewer.setShowTileRect(newShowTileRect);
                        } else {
                            Toast.makeText(activity, "请先到首页左侧菜单开启大图功能", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3:
                        if (supportZoom) {
                            boolean newReadMode = !imageZoomer.isReadMode();
                            imageZoomer.setReadMode(newReadMode);
                        } else {
                            Toast.makeText(activity, "请先到首页左侧菜单开启缩放功能", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 4:
                        if (supportZoom) {
                            if (!imageZoomer.rotateBy(90)) {
                                Toast.makeText(activity, "旋转角度必须是90的倍数或开启大图功能后无法使用旋转功能", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(activity, "请先到首页左侧菜单开启缩放功能", Toast.LENGTH_SHORT).show();
                        }
                }
            }
        });

        builder.setNegativeButton("取消", null);
        tempAlertDialog = builder.show();
    }

    private void showScaleTypeMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("修改ScaleType");

        String[] items = new String[7];
        items[0] = "CENTER";
        items[1] = "CENTER_CROP";
        items[2] = "CENTER_INSIDE";
        items[3] = "FIT_START";
        items[4] = "FIT_CENTER";
        items[5] = "FIT_END";
        items[6] = "FIT_XY";

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tempAlertDialog.dismiss();

                switch (which) {
                    case 0:
                        imageView.setScaleType(ImageView.ScaleType.CENTER);
                        break;
                    case 1:
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        break;
                    case 2:
                        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        break;
                    case 3:
                        imageView.setScaleType(ImageView.ScaleType.FIT_START);
                        break;
                    case 4:
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        break;
                    case 5:
                        imageView.setScaleType(ImageView.ScaleType.FIT_END);
                        break;
                    case 6:
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        break;
                }
            }
        });

        builder.setNegativeButton("取消", null);
        tempAlertDialog = builder.show();
    }

    public void showDetailInfo() {
        Drawable drawable = SketchUtils.getLastDrawable(imageView != null ? imageView.getDrawable() : null);

        if (drawable instanceof LoadingDrawable) {
            Toast.makeText(activity, "正在读取图片，请稍后", Toast.LENGTH_LONG).show();
        } else if (drawable instanceof SketchDrawable) {
            SketchDrawable sketchDrawable = (SketchDrawable) drawable;

            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("URI：").append(sketchDrawable.getUri());
            messageBuilder.append("\n");
            messageBuilder.append("类型：").append(sketchDrawable.getMimeType());
            messageBuilder.append("\n");
            messageBuilder.append("尺寸：").append(sketchDrawable.getOriginWidth()).append("x").append(sketchDrawable.getOriginHeight());

            File image = null;
            UriScheme uriScheme = UriScheme.valueOfUri(sketchDrawable.getUri());
            if (uriScheme == UriScheme.FILE) {
                image = new File(UriScheme.FILE.crop(sketchDrawable.getUri()));
            } else if (uriScheme == UriScheme.NET) {
                DiskCache.Entry diskCacheEntry = Sketch.with(activity).getConfiguration().getDiskCache().get(sketchDrawable.getUri());
                if (diskCacheEntry != null) {
                    image = diskCacheEntry.getFile();
                }
            }
            messageBuilder.append("\n");
            if (image != null) {
                messageBuilder.append("占用空间：").append(Formatter.formatFileSize(activity, image.length()));
            } else {
                messageBuilder.append("占用空间：").append("未知");
            }

            int previewDrawableByteCount = sketchDrawable.getByteCount();
            int pixelByteCount = previewDrawableByteCount / drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();
            int originImageByteCount = sketchDrawable.getOriginWidth() * sketchDrawable.getOriginHeight() * pixelByteCount;
            messageBuilder.append("\n");
            messageBuilder.append("占用内存：").append(Formatter.formatFileSize(activity, originImageByteCount));

            messageBuilder.append("\n");
            messageBuilder.append("\n");
            messageBuilder.append("预览图尺寸：").append(drawable.getIntrinsicWidth()).append("x").append(drawable.getIntrinsicHeight());
            messageBuilder.append("\n");
            messageBuilder.append("预览图Config：").append(sketchDrawable.getBitmapConfig());
            messageBuilder.append("\n");
            messageBuilder.append("预览图占用内存：").append(Formatter.formatFileSize(activity, previewDrawableByteCount));

            if (imageView.isSupportZoom()) {
                ImageZoomer imageZoomer = imageView.getImageZoomer();

                messageBuilder.append("\n");
                messageBuilder.append("\n");
                messageBuilder.append("缩放倍数：").append(SketchUtils.formatFloat(imageZoomer.getZoomScale(), 2));

                messageBuilder.append("\n");
                Rect visibleRect = new Rect();
                imageZoomer.getVisibleRect(visibleRect);
                messageBuilder.append("可见区域：").append(visibleRect.toShortString());
            } else {
                messageBuilder.append("\n");
                messageBuilder.append("\n");
                messageBuilder.append("未开启缩放功能");
            }

            if (imageView.isSupportLargeImage()) {
                messageBuilder.append("\n");
                LargeImageViewer largeImageViewer = imageView.getLargeImageViewer();
                if (largeImageViewer.isReady()) {
                    messageBuilder.append("\n");
                    messageBuilder.append("碎片占用内存：").append(Formatter.formatFileSize(activity, largeImageViewer.getTilesAllocationByteCount()));
                    messageBuilder.append("\n");
                    messageBuilder.append("碎片基数：").append(largeImageViewer.getTiles());
                    messageBuilder.append("\n");
                    messageBuilder.append("碎片数量：").append(largeImageViewer.getTileList().size());
                    messageBuilder.append("\n");
                    messageBuilder.append("解码区域：").append(largeImageViewer.getDecodeRect().toShortString());
                    messageBuilder.append("\n");
                    messageBuilder.append("解码SRC区域：").append(largeImageViewer.getDecodeSrcRect().toShortString());
                } else if (largeImageViewer.isInitializing()) {
                    messageBuilder.append("\n");
                    messageBuilder.append("大图功能正在初始化...");
                } else {
                    messageBuilder.append("\n");
                    messageBuilder.append("无需使用大图功能");
                }
            } else {
                messageBuilder.append("\n");
                messageBuilder.append("\n");
                messageBuilder.append("未开启大图功能");
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(messageBuilder.toString());
            builder.setNegativeButton("取消", null);
            builder.show();
        } else {
            Toast.makeText(activity, "未知来源的图片，无法获取其详细信息", Toast.LENGTH_LONG).show();
        }
    }
}
