package animals.application;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Font;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class UI implements Runnable {
	private ImageView imageView;
	private int petID;
	private EventListener listen;
	private VBox messageBox;
	private CheckboxMenuItem itemWalkable;
	private CheckboxMenuItem autoPlay;
	private CheckboxMenuItem itemSay;
	private MenuItem itemSwitch;
	private Stage primaryStage;
	Thread thread;
	double x;
	String[] lxhStrings= {
			"好无聊。。。",
			"陪我玩会儿吧~",
			"《罗小黑战记》怎么还没更新",
			"想师父了",
			"不就是拿了颗珠子嘛，至于把我打回猫形嘛"
	};
	String[] biuStrings = {
			"想吃东西。。。",
			"biu~",
			"揉揉小肚几",
			"比丢这么可爱，怎么可以欺负比丢"
	};
	public UI(ImageView view, int pet, EventListener el, Stage s) {
		imageView = view;
		petID = pet;
		listen = el;
		primaryStage = s;
	}

	//添加系统托盘
	public void setTray(Stage stage) {
        SystemTray tray = SystemTray.getSystemTray();
        BufferedImage image;//托盘图标
		try {
			// 为托盘添加一个右键弹出菜单
			PopupMenu popMenu = new PopupMenu();
			popMenu.setFont(new Font("微软雅黑", Font.PLAIN,18));

			itemSwitch = new MenuItem("切换宠物");
			itemSwitch.addActionListener(e -> switchPet());

			itemWalkable = new CheckboxMenuItem("自行走动");
			autoPlay = new CheckboxMenuItem("自娱自乐");
			itemSay = new CheckboxMenuItem("碎碎念");
			//令"自行走动"、"自娱自乐"和"碎碎念"不能同时生效
			itemWalkable.addItemListener(il -> {
				if(itemWalkable.getState()) {
					autoPlay.setEnabled(false);
					itemSay.setEnabled(false);
				}
				else {
					autoPlay.setEnabled(true);
					itemSay.setEnabled(true);
				}
			});
			autoPlay.addItemListener(il -> {
				if(autoPlay.getState()) {
					itemWalkable.setEnabled(false);
					itemSay.setEnabled(false);
				}
				else {
					itemWalkable.setEnabled(true);
					itemSay.setEnabled(true);
				}
			});
			itemSay.addItemListener(il -> {
				if(itemSay.getState()) {
					itemWalkable.setEnabled(false);
					autoPlay.setEnabled(false);
				}
				else {
					itemWalkable.setEnabled(true);
					autoPlay.setEnabled(true);
				}
			});

			MenuItem itemShow = new MenuItem("显示");
			itemShow.addActionListener(e -> Platform.runLater(() -> stage.show()));

			MenuItem itemHide = new MenuItem("隐藏");
			//要先setImplicitExit(false)，否则stage.hide()会直接关闭stage
			//stage.hide()等同于stage.close()
			itemHide.addActionListener(e ->{Platform.setImplicitExit(false);
			Platform.runLater(() -> stage.hide());});

			MenuItem itemExit = new MenuItem("退出");
			itemExit.addActionListener(e -> end());

			popMenu.add(itemSwitch);
			popMenu.addSeparator();
			popMenu.add(itemWalkable);
			popMenu.add(autoPlay);
			popMenu.add(itemSay);
			popMenu.addSeparator();
			popMenu.add(itemShow);
			popMenu.add(itemHide);
			popMenu.add(itemExit);
			//设置托盘图标
			image = ImageIO.read(getClass().getResourceAsStream("icon.png"));
			TrayIcon trayIcon = new TrayIcon(image, "桌面宠物", popMenu);
	        trayIcon.setToolTip("桌面宠物");
	        trayIcon.setImageAutoSize(true);//自动调整图片大小。这步很重要，不然显示的是空白
	        tray.add(trayIcon);
		} catch (IOException | AWTException e) {
			e.printStackTrace();
		}
	}

	//切换宠物
	private void switchPet() {
		imageView.removeEventHandler(MouseEvent.MOUSE_CLICKED, listen);//移除原宠物的事件
		//切换宠物ID
		if(petID == 0) {
			petID = 1; //切换成比丢
			imageView.setFitHeight(150);
			imageView.setFitWidth(150);
		}
		else {
			petID = 0; //切换成罗小黑
			imageView.setFitHeight(200);
			imageView.setFitWidth(200);
		}
		//listen = new EventListener(imageView,petID);
		/*
		 * 修改listen.petID是为了修复bug: 在运行三个功能之一时点击切换宠物，图片会切换，但宠物动作不会停止
		 * 且动作完成后恢复的主图还是上一个宠物，直到下一个动作执行才变正常。
		 * 原因在于那三个功能调用listen.loadimg()时传递的是旧petID。
		 */
		listen.petID = petID;
		listen.mainimg(petID,0);//切换至该宠物的主图（图片编号为0）
		//因为listen更新了，所以要重新添加点击事件
		imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, listen);
	}
	//退出程序时展示动画
	public void end() {
		listen.mainimg(petID,99);//播放宠物的告别动画————编号为99的图片
		double time;
		//罗小黑的告别动画1.5秒，比丢的3秒
		if(petID == 0) {
			time = 1.5;
		} else {
			time = 3;
		}
		//要用Platform.runLater，不然会报错Not on FX application thread;
		Platform.runLater(() ->setMsg("�ټ�~"));
		//动画结束后执行退出
		new Timeline(new KeyFrame(
			     Duration.seconds(time),
			     ae ->System.exit(0)))
			    .play();
	}
	//添加聊天气泡
	public void addMessageBox(String message) {
		Label bubble = new Label(message);
		//设置气泡的宽度。如果没有这句，就会根据内容多少来自适应宽度
		bubble.setPrefWidth(100);
        bubble.setWrapText(true);//�Զ�����
        bubble.setStyle("-fx-background-color: DarkTurquoise; -fx-background-radius: 8px;");
        bubble.setPadding(new Insets(7));//��ǩ���ڱ߾�Ŀ��
        bubble.setFont(new javafx.scene.text.Font(14));
        Polygon triangle = new Polygon(
        		0.0, 0.0,
        		8.0, 10.0,
        		16.0, 0.0);//�ֱ��������������������X��Y
        triangle.setFill(Color.DARKTURQUOISE);
        messageBox = new VBox();
//      VBox.setMargin(triangle, new Insets(0, 50, 0, 0));//���������ε�λ�ã�Ĭ�Ͼ���
        messageBox.getChildren().addAll(bubble, triangle);
        messageBox.setAlignment(Pos.BOTTOM_CENTER);
      	messageBox.setStyle("-fx-background:transparent;");
        //��������ڸ�������λ��
        messageBox.setLayoutX(0);
      	messageBox.setLayoutY(0);
      	messageBox.setVisible(true);
      	//�������ݵ���ʾʱ��
      	new Timeline(new KeyFrame(
			     Duration.seconds(8),
			     ae ->{messageBox.setVisible(false);}))
			    .play();
	}

//�ö��߳���ʵ�� �������ʱ����ִ�С��Զ����ߡ����������֡���������Ĺ���
	public void run() {
		while(true) {
			Random rand = new Random();
			//��������Զ��¼����������ü��Ϊ9~24�롣Ҫע�����ʱ���������˶������ŵ�ʱ��
			long time = (rand.nextInt(15)+10)*1000;
			System.out.println("Waiting time:"+time);
			if(itemWalkable.getState() & listen.gifID == 0) {
				walk();
			}
			else if(autoPlay.getState() & listen.gifID == 0) {
				play();
			}
			else if(itemSay.getState() & listen.gifID == 0) {
				//���ѡ��Ҫ˵�Ļ�����ΪĿǰֻ������������Կ�������Ŀ�����
				String str = (petID == 0) ? lxhStrings[rand.nextInt(5)]:biuStrings[rand.nextInt(4)];
				Platform.runLater(() ->setMsg(str));
			}
			try {
				Thread.sleep(time);
			    } catch (InterruptedException e) {
			     e.printStackTrace();
			    }
		}
	}
	/*
	 * ִ��"������"�Ĺ��ܡ����ڳ����Ϸ���ʾ�Ի�����
	 * ��Ĭ�Ͽ����ǿ��ǵ��û����ܲ��뱻����
	 */
	public void setMsg(String msg) {

		Label lbl = (Label) messageBox.getChildren().get(0);
      	lbl.setText(msg);
      	messageBox.setVisible(true);
      	//�������ݵ���ʾʱ��
      	new Timeline(new KeyFrame(
			     Duration.seconds(4),
			     ae ->{messageBox.setVisible(false);}))
			    .play();
	}

	/*
	 * ִ��"�����߶�"�Ĺ��ܡ�����ˮƽ�������߶�
	 * ��Ĭ�Ͽ����ǿ��ǵ��û�����ֻ����ﰲ������
	 */
	void walk(){
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		x = primaryStage.getX();//stage�����Ե����
		double maxx = screenBounds.getMaxX();//��ȡ��Ļ�Ĵ�С
		double width = imageView.getBoundsInLocal().getWidth();//��ȡimageView�Ŀ�ȣ�Ҳ��ʹ��.getMaxX();
		Random rand = new Random();
		double speed=10;//ÿ���ƶ��ľ���
		//�����Ҫ������Ļ��Ե��ͣ��
        if(x+speed+width >= maxx | x-speed<=0)
        	return;
        //��������ƶ���ʱ�䣬��λ΢��ms
		long time = (rand.nextInt(4)+3)*1000;
		System.out.println("Walking time:"+time);
		int direID = rand.nextInt(2);//�����������0Ϊ��1Ϊ��
		//�л�����Ӧ���������ͼ
		Image newimage;
		if(petID == 0)
			newimage = new Image(this.getClass().getResourceAsStream("/lxh/��С��w"+direID+".gif"));
		else {
			newimage = new Image(this.getClass().getResourceAsStream("/biu/biuw"+direID+".gif"));
		}
		imageView.setImage(newimage);
		//�ƶ�
		Move move = new Move(time, imageView, direID, primaryStage, listen);
		thread = new Thread(move);
		thread.start();
	}
	/*
	 * ִ��"��������"�Ĺ��ܡ�������ʱ���������
	 * �����Ͳ����ܲ�λ���������ƣ�Ҳ�����ó����Եô���
	 * ��Ĭ�Ͽ����ǿ��ǵ��û�����ֻ����ﰲ������
	 */
	void play() {
		Random rand = new Random();
		int gifID;
		double time = 4;
		//gifID�Ǹ���ͼƬ�ļ�������;δ�����ͼƬ�����趨�Ķ���������ȷ����
		if(petID == 0) {
			gifID = rand.nextInt(7)+5;
		}
		else
			gifID = rand.nextInt(7)+7;
		listen.loadImg(petID, gifID, time);
	}
	public ImageView getImageView() {
		return imageView;
	}

	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}

	public VBox getMessageBox() {
		return messageBox;
	}

	public void setMessageBox(VBox messageBox) {
		this.messageBox = messageBox;
	}
}
