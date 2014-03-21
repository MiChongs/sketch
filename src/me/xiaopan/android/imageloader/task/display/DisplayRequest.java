/*
 * Copyright (C) 2013 Peng fei Pan <sky@xiaopan.me>
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

package me.xiaopan.android.imageloader.task.display;

import me.xiaopan.android.imageloader.task.load.LoadRequest;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

/**
 * 显示请求
 */
public class DisplayRequest extends LoadRequest{
	private String id;	//ID
	private DisplayListener displayListener;	//监听器
	private DisplayOptions displayOptions;	//显示选项
	private ImageViewHolder imageViewHolder;	//ImageView持有器
    private ImageView.ScaleType scaleType;  //缩放方式
	
	public DisplayRequest(String id, String uri) {
		super(uri);
		setId(id);
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public DisplayListener getDisplayListener() {
		return displayListener;
	}

	public void setDisplayListener(DisplayListener displayListener) {
		this.displayListener = displayListener;
	}
	
	public DisplayOptions getDisplayOptions() {
		return displayOptions;
	}

	public void setDisplayOptions(DisplayOptions displayOptions) {
		this.displayOptions = displayOptions;
		setLoadOptions(displayOptions);
	}
	
	public ImageViewHolder getImageViewHolder() {
		return imageViewHolder;
	}

	public void setImageViewHolder(ImageViewHolder imageViewHolder) {
		this.imageViewHolder = imageViewHolder;
	}

    @Override
    public ImageView.ScaleType getScaleType() {
        return scaleType;
    }

    public void setScaleType(ImageView.ScaleType scaleType) {
        this.scaleType = scaleType;
    }

    public interface DisplayListener {
		public void onStarted(String imageUri, ImageView imageView);
		public void onFailed(String imageUri, ImageView imageView);
		public void onComplete(String imageUri, ImageView imageView, BitmapDrawable drawable);
		public void onCancelled(String imageUri, ImageView imageView);
	}

	@Override
	public boolean isEnableDiskCache() {
		return displayOptions != null?displayOptions.isEnableDiskCache():false;
	}

	@Override
	public int getDiskCachePeriodOfValidity() {
		return displayOptions != null?displayOptions.getDiskCachePeriodOfValidity():0;
	}
}
