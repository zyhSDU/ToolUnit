package helper.scxml.scxml2.t1_stopWatch.stopWatch1;

import helper.base.DebugHelper;
import helper.base.ResourceHelper;
import helper.scxml.scxml2.t1_stopWatch.StopWatchEntity;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.env.SimpleErrorReporter;
import org.apache.commons.scxml2.env.jexl.JexlEvaluator;
import org.apache.commons.scxml2.io.SCXMLReader;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.SCXML;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

public class StopWatchFrame extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    private JLabel displayLabel;

    private JButton startButton;
    private JButton stopButton;
    private JButton resetButton;

    private SCXMLExecutor executor;
    private StopWatchEntity stopWatchEntity;

    public static void main(String[] args) {

        new StopWatchFrame();
    }

    public StopWatchFrame() {
        super("SCXML StopWatch");
        //初始化状态机
        initStopWatch();
        //初始化界面
        initUI();
    }

    /**
     * 监听器需要执行的方法，自动调用
     *
     * @param event 事件源
     */
    public void actionPerformed(ActionEvent event) {
        //得到绑定在每个按钮上的命令
        String command = event.getActionCommand();
        //对各个命令进行判断，在执行相应的内容
        try {
            if ("START".equals(command)) {
                //生成watch.start事件，将转到running状态
                executor.triggerEvent(new TriggerEvent("watch.start", TriggerEvent.SIGNAL_EVENT));
                //设置一些列按钮的可见性
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                resetButton.setEnabled(false);

            } else if ("STOP".equals(command)) {
                //生成watch.stop事件，将转到stoped状态
                executor.triggerEvent(new TriggerEvent("watch.stop", TriggerEvent.SIGNAL_EVENT));

                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                resetButton.setEnabled(true);

            } else if ("RESET".equals(command)) {
                //生成watch.reset事件，将转到reset状态
                executor.triggerEvent(new TriggerEvent("watch.reset", TriggerEvent.SIGNAL_EVENT));

                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                resetButton.setEnabled(false);

            }
        } catch (ModelException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化秒表
     */
    private void initStopWatch() {
        //实例化数据模型解析器
        Evaluator evaluator = new JexlEvaluator();

        //实例化引擎
        executor = new SCXMLExecutor(evaluator, null, new SimpleErrorReporter());

        try {
            //加载资源文件,实例化到一个SCXML对象，两者之间一一对应
            SCXML scxml = SCXMLReader.read(ResourceHelper.INSTANCE.getResource(
                    "scxml2/stopwatch1.scxml",
                    DebugHelper.DebuggerList.Companion.getDebuggerList(0)
            ));

            //将这样的一个SCXML实例，作为状态机对象，传入到引擎里面。
            executor.setStateMachine(scxml);

            //设置引擎执行的根上下文
            Context rootContext = evaluator.newContext(null);
            final StopWatchEntity stopWatchEntity = new StopWatchEntity();
            rootContext.set("stopWatchEntity", stopWatchEntity);
            executor.setRootContext(rootContext);

            //设置当前对象
            this.stopWatchEntity = stopWatchEntity;

            //开始启动流程
            executor.go();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 初始化界面
     */
    private void initUI() {

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new FlowLayout());
        displayLabel = new JLabel("0:00:00,000");
        displayLabel.setFont(new Font(Font.DIALOG, 100, 50));
        contentPanel.add(displayLabel, BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        startButton = createButton("START", "Start");
        buttonPanel.add(startButton);

        stopButton = createButton("STOP", "Stop");
        stopButton.setEnabled(false);
        buttonPanel.add(stopButton);

        resetButton = createButton("RESET", "Reset");
        resetButton.setEnabled(false);
        buttonPanel.add(resetButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);


        setLocation(250, 300);
        setSize(400, 200);

        setResizable(true);
        setVisible(true);


        Timer displayTimer = new Timer();

        displayTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                displayLabel.setText(stopWatchEntity.getDisplay());
            }
        }, 100, 100);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

    }

    /**
     * 创建一个按钮
     *
     * @param command 按钮的命令
     * @param text    按钮上的文本
     * @return 返回一个新建的按钮
     */
    private JButton createButton(final String command, final String text) {
        JButton button = new JButton(text);
        button.setActionCommand(command);
        button.addActionListener(this);
        return button;
    }

}