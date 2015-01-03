package com.avoscloud.chat.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.avoscloud.chat.adapter.RecentMessageAdapter;
import com.avoscloud.chat.entity.Conversation;
import com.avoscloud.chat.entity.RoomType;
import com.avoscloud.chat.service.ChatService;
import com.avoscloud.chat.service.listener.MsgListener;
import com.avoscloud.chat.service.receiver.GroupMsgReceiver;
import com.avoscloud.chat.service.receiver.MsgReceiver;
import com.avoscloud.chat.ui.activity.ChatActivity;
import com.avoscloud.chat.ui.view.xlist.XListView;
import com.avoscloud.chat.util.NetAsyncTask;
import com.avoscloud.chat.R;
import com.avoscloud.chat.util.Utils;

import java.util.List;

/**
 * Created by lzw on 14-9-17.
 */
public class ConversationFragment extends BaseFragment implements AdapterView.OnItemClickListener, MsgListener, XListView.IXListViewListener {
  XListView xListView;
  RecentMessageAdapter adapter;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.message_fragment, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    initView();
    onRefresh();
  }

  private void initView() {
    headerLayout.showTitle(R.string.messages);
    xListView = (XListView) getView().findViewById(R.id.convList);
    xListView.setPullLoadEnable(false);
    xListView.setPullRefreshEnable(true);
    xListView.setXListViewListener(this);
    adapter = new RecentMessageAdapter(getActivity());
    xListView.setAdapter(adapter);
    xListView.setOnItemClickListener(this);
  }

  @Override
  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
    // TODO Auto-generated method stub
    Conversation recent = (Conversation) adapter.getItem(position);
    if (recent.getMsg().getRoomType() == RoomType.Single) {
      ChatActivity.goUserChat(getActivity(), recent.getToUser().getObjectId());
    } else {
      ChatActivity.goGroupChat(getActivity(), recent.getToChatGroup().getObjectId());
    }
  }

  private boolean hidden;

  @Override
  public void onHiddenChanged(boolean hidden) {
    super.onHiddenChanged(hidden);
    this.hidden = hidden;
    if (!hidden) {
      onRefresh();
    }
  }


  @Override
  public void onResume() {
    super.onResume();
    if (!hidden) {
      onRefresh();
    }
    GroupMsgReceiver.addMsgListener(this);
    MsgReceiver.addMsgListener(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    MsgReceiver.removeMsgListener(this);
    GroupMsgReceiver.removeMsgListener(this);
  }

  @Override
  public boolean onMessageUpdate(String otherId) {
    onRefresh();
    return false;
  }

  @Override
  public void onRefresh() {
    new GetDataTask(ctx, false).execute();
  }

  @Override
  public void onLoadMore() {

  }

  class GetDataTask extends NetAsyncTask {
    List<Conversation> conversations;

    GetDataTask(Context cxt, boolean openDialog) {
      super(cxt, openDialog);
    }

    @Override
    protected void doInBack() throws Exception {
      conversations = ChatService.getConversationsAndCache();
    }

    @Override
    protected void onPost(Exception e) {
      xListView.stopRefresh();
      if (e != null) {
        Utils.toast(ctx, R.string.pleaseCheckNetwork);
      } else {
        adapter.setDatas(conversations);
        adapter.notifyDataSetChanged();
      }
    }
  }
}
