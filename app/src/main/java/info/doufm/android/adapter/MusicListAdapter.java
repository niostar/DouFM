package info.doufm.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import info.doufm.android.R;

/**
 * Created by WJ on 2015/1/27.
 */
public class MusicListAdapter extends BaseAdapter {

    private List<String> leftMusicList;
    private Context context;
    private LayoutInflater layoutInflater;

    public MusicListAdapter(Context context,List<String> leftMusicList) {
        this.context = context;
        this.leftMusicList = leftMusicList;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return leftMusicList.size();
    }

    @Override
    public Object getItem(int position) {
        return leftMusicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemHoler holer;
        if(convertView == null){
            holer = new ItemHoler();
            convertView = layoutInflater.inflate(R.layout.music_list_item,parent,false);
            holer.imageView = (ImageView) convertView.findViewById(R.id.iv_channal_icon);
            holer.textView = (TextView) convertView.findViewById(R.id.tv_channal_name);
            convertView.setTag(holer);
        }else{
            holer = (ItemHoler) convertView.getTag();
        }

        holer.textView.setText(leftMusicList.get(position));

        return convertView;
    }


    private class ItemHoler{
       ImageView imageView;
       TextView textView;
    }


}
