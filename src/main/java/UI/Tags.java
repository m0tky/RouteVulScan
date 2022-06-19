package UI;

import burp.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Tags extends AbstractTableModel implements ITab, IMessageEditorController {
    private IBurpExtenderCallbacks callbacks;

    private JSplitPane top;

    private List<TablesData> Udatas = new ArrayList<>();

    private IMessageEditor HRequestTextEditor;

    private IMessageEditor HResponseTextEditor;

    private IHttpRequestResponse currentlyDisplayedItem;

    private URLTable Utable;

    private JScrollPane UscrollPane;

    private JSplitPane HjSplitPane;

    private JTabbedPane Ltable;

    private JTabbedPane Rtable;

    private JSplitPane splitPane;
    public List<String> Get_URL_list(){
        List<String> Urls = new ArrayList<>();
        for (TablesData data : this.Udatas) {
            Urls.add(data.url);
        }
        return Urls;
    }

    public Tags(IBurpExtenderCallbacks callbacks, Config Config_l) {
        this.callbacks = callbacks;
//        this.tagName = name;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // 创建最上面的一层
                Tags.this.top = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                // 创建容器，容器可以加入多个页面
                JTabbedPane tabs = new JTabbedPane();
                // 创建主拆分窗格
                splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

                // 日志条目表
                Tags.this.Utable = new URLTable(Tags.this);
                Tags.this.UscrollPane = new JScrollPane(Tags.this.Utable);

                //创建请求和响应的展示窗
                Tags.this.HjSplitPane = new JSplitPane();
                Tags.this.HjSplitPane.setDividerLocation(0.5D);

                // 创建请求/响应的子选项卡
                Tags.this.Ltable = new JTabbedPane();
                Tags.this.Rtable = new JTabbedPane();
                Tags.this.HRequestTextEditor = Tags.this.callbacks.createMessageEditor(Tags.this, false);
                Tags.this.HResponseTextEditor = Tags.this.callbacks.createMessageEditor(Tags.this, false);
                Tags.this.Ltable.addTab("Request", Tags.this.HRequestTextEditor.getComponent());
                Tags.this.Rtable.addTab("Response", Tags.this.HResponseTextEditor.getComponent());

                // 将子选项卡添加进主选项卡
                Tags.this.HjSplitPane.add(Tags.this.Ltable, "left");
                Tags.this.HjSplitPane.add(Tags.this.Rtable, "right");

                // 将日志条目表和展示窗添加到主拆分窗格
                Tags.this.splitPane.add(Tags.this.UscrollPane, "left");
                Tags.this.splitPane.add(Tags.this.HjSplitPane, "right");

                // 将两个页面插入容器
                tabs.addTab("VulDisplay",Tags.this.splitPane);
                tabs.addTab("config",Config_l.$$$getRootComponent$$$());

                // 将容器置于顶层
                top.setTopComponent(tabs);

                // 定制我们的UI组件
                Tags.this.callbacks.customizeUiComponent(Tags.this.top);

                // 将自定义选项卡添加到Burp的UI
                Tags.this.callbacks.addSuiteTab(Tags.this);
            }
        });
    }

    public String getTabCaption() {
        return "RouteVulScan";
    }

    public Component getUiComponent() {
        return this.top;
    }

    public int getRowCount() {
        return this.Udatas.size();
    }

    public int getColumnCount() {
        return 9;
    }

    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "#";
            case 1:
                return "VulName";
            case 2:
                return "Method";
            case 3:
                return "Url";
            case 4:
                return "Status";
            case 5:
                return "Info";
            case 6:
                return "Size";
            case 7:
                return "startTime";
            case 8:
                return "endTime";
        }
        return null;
    }

    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        TablesData datas = this.Udatas.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return Integer.valueOf(datas.id);
            case 1:
                return datas.VulName;
            case 2:
                return datas.Method;
            case 3:
                return datas.url;
            case 4:
                return datas.status;
            case 5:
                return datas.Info;
            case 6:
                return datas.Size;
            case 7:
                return datas.startTime;
            case 8:
                return datas.endTime;
        }
        return null;
    }

    public byte[] getRequest() {
        return this.currentlyDisplayedItem.getRequest();
    }

    public byte[] getResponse() {
        return this.currentlyDisplayedItem.getResponse();
    }

    public IHttpService getHttpService() {
        return this.currentlyDisplayedItem.getHttpService();
    }

    public int add(String VulName, String Method, String url, String status, String Info,String Size, IHttpRequestResponse requestResponse) {
        synchronized (this.Udatas) {
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime = sdf.format(d);
            int id = this.Udatas.size();
            this.Udatas.add(
                    new TablesData(
                            id,
                            VulName,
                            Method,
                            url,
                            status,
                            Info,
                            Size,
                            requestResponse,
                            startTime,
                            ""));
            fireTableRowsInserted(id, id);
            return id;
        }
    }


    public class URLTable extends JTable {
        public URLTable(TableModel tableModel) {
            super(tableModel);
        }

        public void changeSelection(int row, int col, boolean toggle, boolean extend) {
            TablesData dataEntry = Tags.this.Udatas.get(convertRowIndexToModel(row));
            Tags.this.HRequestTextEditor.setMessage(dataEntry.requestResponse.getRequest(), true);
            Tags.this.HResponseTextEditor.setMessage(dataEntry.requestResponse.getResponse(), false);
            Tags.this.currentlyDisplayedItem = dataEntry.requestResponse;
            super.changeSelection(row, col, toggle, extend);
        }
    }

    public static class TablesData {
        final int id;

        final String VulName;

        final String Method;

        final String url;

        final String status;

        final String Info;

        final String Size;

        final IHttpRequestResponse requestResponse;

        final String startTime;

        final String endTime;

        public TablesData(int id, String VulName, String Method, String url, String status, String Info,String Size, IHttpRequestResponse requestResponse, String startTime, String endTime) {
            this.id = id;
            this.VulName = VulName;
            this.Method = Method;
            this.url = url;
            this.status = status;
            this.Info = Info;
            this.Size = Size;
            this.requestResponse = requestResponse;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}


