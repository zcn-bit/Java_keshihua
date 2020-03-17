package lab;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
public class CountDownLatchDemo {
    private static class Job implements Runnable {
        private CountDownLatch countDownLatch;

        Job(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            //模拟工作耗时（随机耗时）
            Random random = new Random();
            try {
                Thread.sleep(random.nextInt(30) * 1000);//30s
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();//计数下载，完成的线程
            System.out.println("一个线程的任务结束了");
        }//通过计数的方法判断任务是否完成
    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(10);//10个任务全部结束才可以   下载占有
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(new Job(countDownLatch));
            thread.start();
        }

        System.out.println("等待 10 个线程全部结束");
        countDownLatch.await();//等的线程
        System.out.println("10 个线程全部结束了");
    }
}
