package gene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//ÿ����ĸ�ʽ
class Point {
	public ArrayList<Float> point;
	//-2��ʾ��ʼ��ÿ���㣬��û��ָ���ĸ���
	int flag = -2;
}

public class Kcluster {
	ArrayList<Point> pointList = null;// �㼯
	ArrayList<Point> pacore = null;// old��������
	ArrayList<Point> pacoren = null;// new��������
	final int pointNum = 200000;
	final int pointSize = 4;
	final int core = 4; // ��ʼ����������Ŀ
	final int cpuCoreNum = 4;
	ExecutorService service = Executors.newFixedThreadPool(cpuCoreNum);
	ComputeTask t1 = new ComputeTask(0, 49999);
    ComputeTask t2 = new ComputeTask(50000, 99999);
    ComputeTask t3 = new ComputeTask(100000, 149999);
    ComputeTask t4 = new ComputeTask(150000, 199999);
    long start = System.currentTimeMillis();
    long end;

	// ���Ծ������ģ��㼯
	public void initCore() {
		Random rand = new Random();
		// ��ʼ����������λ��
		this.pacore = new ArrayList<Point>();// ��ž�������
		this.pacoren = new ArrayList<Point>();
		for (int i = 0; i < core; i++) {
			Point lists = new Point();
			lists.point = new ArrayList<Float>();
			for (int j = 0; j < pointSize; ++j) {
				lists.point.add(rand.nextFloat());
			}
			pacore.add(lists);
			pacoren.add(lists);
		}
		System.out.println("��ʼ�������ģ�");
		for (int i = 0; i < pacore.size(); i++) {
			for (int j = 0; j < pointSize; ++j) {
				System.out.print(pacore.get(i).point.get(j) + " ");
			}
			System.out.println();
		}
	}


    ///�ҳ�ÿ���������ĸ���������
    public void searchbelong(int startPos, int endPos)// �ҳ�ÿ���������ĸ���������
    {
        for (int i = startPos; i < endPos; i++) {
            double dist = 999;
            int label = -1;
            for (int j = 0; j < pacore.size(); j++) {
                double distance = distpoint(pointList.get(i), pacore.get(j));
                if (distance < dist) {
                    dist = distance;
                    label = j;
                }
            }
            pointList.get(i).flag = label;
            //System.out.printf("%d belongs to %d\n",i, pointList.get(i).flag);
        }
    }

    // ���¾�������
    public void updateCore() {
        for (int i = 0; i < core; i++) {
            System.out.print("��<" + pacore.get(i).point  + ">Ϊ���ĵĵ㣺");
            int numc = 0;
            Point newcore = new Point();
            newcore.point = new ArrayList<Float>();
            for (int j = 0; j < pointList.size(); j++) {
                if (pointList.get(j).flag == i) {
                    System.out.print(pacore.get(i));
                    numc += 1;
                    for(int k = 0; k < pointSize; ++k) {
                    	newcore.point.add(0.0F);
                    	newcore.point.set(k, pointList.get(j).point.get(k) + newcore.point.get(k));
                    } 
                }
            }
            // �µľ�������
            System.out.println(pacoren.size());
            for(int z = 0; z < pointSize; ++z) {
            	pacoren.get(i).point.set(z, newcore.point.get(z) / numc );
            }
            //-1��ʾ�ص�����
            pacoren.get(i).flag = -1;
            System.out.println("�µľ������ģ�" + pacoren.get(i));
        }
    }

    public double distpoint(Point ppoint, Point pcore) {
    	double sum = 0;
    	for(int i = 0; i < pointSize; ++i) {
        sum += Math.sqrt(Math.pow((ppoint.point.get(i) - pcore.point.get(i)), 2));
    	}
    	return sum;
    }

    public void change_oldToNew(ArrayList<Point> old, ArrayList<Point> news) {
        for (int i = 0; i < news.size(); i++) {
        	for(int j = 0; j < pointSize ; ++j) {
            old.get(i).point.set(j, news.get(i).point.get(j));
            if(old.get(i).point.get(j) == news.get(i).point.get(j)) {
            	System.out.println("true");
            }
            }
        	old.get(i).flag = -1;//��ʾ��������
        }
    }
    
	public ArrayList<Point> readKcluster() {
		ArrayList<Point> pointList = new ArrayList<Point>();
		try { // ��ֹ�ļ��������ȡʧ�ܣ���catch��׽���󲢴�ӡ��Ҳ����throw
			/* ����TXT�ļ� */
			String pathname = ".//geneData.txt"; // ����·�������·�������ԣ������Ǿ���·����д���ļ�ʱ��ʾ���·��
			File filename = new File(pathname); // Ҫ��ȡ����·����input��txt�ļ�
			InputStreamReader reader = new InputStreamReader(new FileInputStream(filename)); // ����һ������������reader
			BufferedReader br = new BufferedReader(reader); // ����һ�����������ļ�����ת�ɼ�����ܶ���������
			String line = "";
			line = br.readLine();
			while (line != null) {
				String[] strings = line.split(" ");
				Point p = new Point();
				p.point = new ArrayList<Float>();
				for (int i = 0; i < pointSize; ++i) {
					p.point.add(Float.parseFloat(strings[i]));
				}
				pointList.add(p);
				line = br.readLine(); // һ�ζ���һ������
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pointList;
	}

    public void movecore() {
    	this.initCore();
		Future<Boolean> f1 = service.submit(t1);
        Future<Boolean> f2 = service.submit(t2);
        Future<Boolean> f3 = service.submit(t3);
        Future<Boolean> f4 = service.submit(t4);
        try {
			f1.get();
	        f2.get();
	        f3.get();
	        f4.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		this.updateCore();
        double movedistance = 0;
        int biao = -1;//��־���������ĵ���ƶ��Ƿ������С����
        for (int i = 0; i < pacore.size(); i++) {
            movedistance = distpoint(pacore.get(i), pacoren.get(i));
            System.out.println("distcore:" + movedistance);//�������ĵ��ƶ�����
            if (movedistance < 0.01) {
                biao = 0;
            } else {
                biao=1;//��Ҫ����������
                break;
            }
        }
        if (biao == 0) {
            System.out.println("������ϣ���������");
	        service.shutdown();
	        if(service.isShutdown()) {
	        	end = System.currentTimeMillis();
	        	System.out.println("��ʱ " + (end - start) + "ms");
	        }
        } else {
        	this.change_oldToNew(this.pacore, this.pacoren);
            movecore();
        }
    }
    
	public static void main(String[] args) {	
		Kcluster kmean = new Kcluster();
		kmean.pointList = kmean.readKcluster();
		kmean.movecore();
	}
	
	 class ComputeTask implements Callable<Boolean> {
	    private int start, end;
	    ComputeTask (int start, int end) {
	        this.start = start;
	        this.end = end;
	    }
		@Override
		public Boolean call() throws Exception{
			searchbelong(start, end);
			return true;
		}
	 }
}
