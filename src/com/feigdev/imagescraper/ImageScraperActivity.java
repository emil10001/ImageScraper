package com.feigdev.imagescraper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ImageScraperActivity extends Activity {
	private static final String TAG = "ImageScraperActivity";
	private ArrayList<ResultsHolder> rh;
	private MessageAdapter listAdapter; 
	private ListView listView;
	private int imgWidth;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        rh = new ArrayList<ResultsHolder>();
        
		listView = (ListView)findViewById(R.id.results_list);
        listAdapter = new MessageAdapter(ImageScraperActivity.this);
        listView.setAdapter(listAdapter);
        

    }
    

    private void request(String url){
    	try {
			Document doc = Jsoup.connect(url).get();
			Log.d(TAG,"request result: " + doc.toString());
			Elements images = doc.getElementsByTag("img");
			ResultsHolder result;
			for (Element image : images){
				Log.d(TAG,"image: " + image.attr("src").toString());
				result = new ResultsHolder(image.attr("src"));
				if (null != result.getImage()){
					rh.add(result);
				}
				else {
					result = new ResultsHolder(url + "/" + image.attr("src"));
					if (null != result.getImage()){
						rh.add(result);
					}
				}
				
			}
	        
			listAdapter.notifyDataSetChanged();
			listAdapter.notifyDataSetInvalidated();
			listView.invalidateViews();
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    @Override
    public void onStart(){
    	super.onStart();
    	imgWidth = (listView.getWidth() *2/3);
        if (0 == imgWidth){
        	imgWidth = 200;
        }
    	if (this.getIntent() != null){
    		onNewIntent(this.getIntent());
    	}
    }
    
    protected void onNewIntent(Intent intent) {
    	 super.onNewIntent(intent);
    	 String uri = null; 
         String title = null; 

         Log.d(TAG, "onNewIntent called: " + intent.toString());
         Log.d(TAG, "Intent type: " + intent.getType());
         Log.d(TAG, "Intent categories: " + intent.getCategories());
         
         // launcher intent
         if (null == intent.getType()){
        	 Log.d(TAG, "intent type is null, returning");
        	 return;
         }
         // from the browser
         else if (intent.getType().equals("text/plain")){
        	 uri = intent.getStringExtra(Intent.EXTRA_TEXT); 
	         title = intent.getStringExtra(Intent.EXTRA_SUBJECT); 
	         Log.d(TAG,"uri=" + uri + ", title=" + title);
	         
	         if (null != uri){
	        	 request(uri);
	         }
	         else {
	        	 Log.d(TAG,"uri is null - exiting");
	        	 finish();
	         }
         }
    }
    
    private class MessageAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		protected MessageViewHolder messageHolder;
		
		public MessageAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			return rh.size();
		}
		 
		@Override
		public Object getItem(int position) {
			return position;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			convertView = mInflater.inflate(R.layout.result, null);
			
			messageHolder = new MessageViewHolder();
			messageHolder.url = (TextView) convertView.findViewById(R.id.src_text);
			messageHolder.size = (TextView) convertView.findViewById(R.id.size_text);
			messageHolder.image = (ImageView) convertView.findViewById(R.id.imageView1);
			
			convertView.setTag(messageHolder);
			
			messageHolder.url.setText(rh.get(position).getUrl());
			messageHolder.size.setText(rh.get(position).getSize());
			messageHolder.image.setImageBitmap(rh.get(position).getImage());
			
			convertView.setOnClickListener(new View.OnClickListener() {
            	
                @Override
                public void onClick(View view) {
        			Log.d(TAG,"item clicked: " + rh.get(position).getUrl());
                }

              });
			return convertView;
		}

	}
    
    protected class ResultsHolder{
    	private Bitmap image;
    	private String size;
    	private String url;
    	
    	public ResultsHolder(String url){
    		this.url =  url;
    		genImage();
    	}
    	
		public Bitmap getImage() {
			return image;
		}
		public void genImage() {
			image = generatePic(url);
			if (null != image){
				size = image.getWidth() + "x" + image.getHeight();
			}
			return;
		}
		public String getSize() {
			return size;
		}
		public String getUrl() {
			return url;
		}
    }

	public Bitmap generatePic(String picture){
		try {
			Bitmap bmp;
			URL url;
			if (picture.equals("") || picture.equals("null") || picture.equals(null)){
				return null;
			}
			else {
				url = new URL(picture);
				URLConnection conn = url.openConnection();
				conn.connect();
				InputStream instream = conn.getInputStream();
				BitmapFactory.Options options=new BitmapFactory.Options();
				options.inSampleSize = 2;
				bmp = BitmapFactory.decodeStream(instream, null, options);
				if (bmp == null){
					return null;
				}
				int width = bmp.getWidth();
	            int height = bmp.getHeight();
	            
	            int newWidth = imgWidth;
	            
	            float scaleWidth = ((float) newWidth) / width;
		        float newHeight = height * scaleWidth;
	            float scaleHeight = ((float) newHeight) / height;
	            
	            Matrix matrix = new Matrix();
	            matrix.postScale(scaleWidth, scaleHeight);
		             
	            bmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
	            instream.close();
	            
	            return bmp;
			}
		}catch (Exception e) {
				Log.w(TAG, "getPic blew up =(");
				e.printStackTrace();
		} 
		
		return null;
		
		
	}
	
	protected class MessageViewHolder {
		TextView url;
		TextView size;
		ImageView image;
		
	}
        
}