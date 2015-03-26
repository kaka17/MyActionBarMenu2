package com.kaiser.aaa.myactionbarmenu.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.kaiser.aaa.myactionbarmenu.R;
import com.kaiser.aaa.myactionbarmenu.Utils.HttpHelper;
import com.kaiser.aaa.myactionbarmenu.Utils.ParserJSONUtils;
import com.kaiser.aaa.myactionbarmenu.Utils.PathHelper;
import com.kaiser.aaa.myactionbarmenu.adapter.Content_Viewpager_Adapter;
import com.kaiser.aaa.myactionbarmenu.entity.ContentBean;
import com.kaiser.aaa.myactionbarmenu.interfaces.CallBackJSONStr;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@ContentView(R.layout.activity_content)
public class Content extends ActionBarActivity {

   private MapView mapView_content;
    private BaiduMap mBaiduMap;
    //内容详细的顶部Viewpager
    @ViewInject(R.id.viewpager_content)
    private ViewPager viewpager_content;
    @ViewInject(R.id.scrollView_content)
    private ScrollView scrollView_content;
    @ViewInject(R.id.layout_content_top)
    private LinearLayout layout_content_top;
    //顶部Viewpager的数据源
    private List<View> list_view=new ArrayList<>();
    private Content_Viewpager_Adapter adapter=null;
    private PoiSearch poiSearch = null;
    private List<ContentBean> list_cBean=new ArrayList<>();
    private String id;
    private float myalph=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 在使用SDK各组件之前初始化context信息，传入ApplicationContext
         // 注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        checkKey();
         // 装载布局文件
        ViewUtils.inject(this);
        mapView_content= (MapView) findViewById(R.id.mapView_content);
        mBaiduMap=mapView_content.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);

        Bundle bundle=getIntent().getExtras();
        id= bundle.getString("id");

//        poiSearch = PoiSearch.newInstance();
//
//        poiSearch
//                .setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
//
//                    @Override
//                    public void onGetPoiResult(PoiResult result) {
//                        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
//                            mBaiduMap.clear();// 刷新百度地图
//                            // 给泡泡添加数据
//                            PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
//                            overlay.setData(result);
//                            overlay.addToMap();// 添加到地图上。
//                            // 给泡泡监听
//                            mBaiduMap.setOnMarkerClickListener(overlay);
//                        }
//                    }
//
//                    @Override
//                    public void onGetPoiDetailResult(PoiDetailResult arg0) {
//
//                    }
//                });
        //加载viewPager的数据
        initView();

        scrollView_content.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_MOVE){
                    myalph=myalph-0.01f;
                    layout_content_top.setAlpha(myalph);
                    //可以监听到ScrollView的滚动事件

                }
                return false;
            }
        });

    }
    class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            // 点击第几个泡泡就返回第几个泡泡的详情。
            PoiInfo info = getPoiResult().getAllPoi().get(index);
            if (info.hasCaterDetails) {// 说明点击开以点开
                // 设置明细
                poiSearch.searchPoiDetail(new PoiDetailSearchOption()
                        .poiUid(info.uid));
            }
            return true;

        }
    }

    public void initView(){ //加载viewPager的数据
        HttpHelper.getJSONStr(PathHelper.contentpath(id),new CallBackJSONStr() {
            @Override
            public void getJSONStr(String jsonStr) {
                List<JSONObject> list_json = ParserJSONUtils.parserContentJson(jsonStr);
                for (int i = 0; i <list_json.size() ; i++) {
                    ContentBean b=new ContentBean();
                    b.getPareseJson(list_json.get(i));
                    //装图片的容器
                    ImageView view=new ImageView(getApplicationContext());
                    view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    view.setBackgroundResource(R.drawable.loadingimg);
                   // view.setScaleType(ImageView.ScaleType.FIT_XY);
                    // 加载图片
                    HttpHelper.getBitmapUtils(getApplicationContext()).display(view,b.getNewPath());
                    list_view.add(view);
                }
                adapter.notifyDataSetChanged();
            }
        });
        adapter=new Content_Viewpager_Adapter(list_view);
        viewpager_content.setAdapter(adapter);
    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mapView_content.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView_content.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView_content.onDestroy();
    }



    private void checkKey() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter
                .addAction(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE);
        //动态注册广播。
        registerReceiver(new MyReceiver(), intentFilter);
    }

    // 广播
    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 广播监听 key是否错误。
            if (intent
                    .getAction()
                    .equals(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE)) {
                setTitle("秘钥错误，无法正常加载地图");

            }

        }

    }
}