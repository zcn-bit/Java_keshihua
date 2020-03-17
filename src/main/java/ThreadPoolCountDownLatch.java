import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;

import javax.sql.DataSource;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ThreadPoolCountDownLatch {

        private static class Job implements Runnable {

            private String url;//别人拼接好的

            private DataSource dataSource;
            private CountDownLatch countDownLatch;

            public Job(String url,DataSource dataSource,CountDownLatch countDownLatch) {
                this.url = url;

                this.dataSource = dataSource;
                this.countDownLatch=countDownLatch;
            }

            public void run() {
                WebClient client = new WebClient(BrowserVersion.CHROME);

                client.getOptions().setJavaScriptEnabled(false);//得到选择，期权
                client.getOptions().setCssEnabled(false);
                try {
                    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");//也是线程不安全的
                    HtmlPage page = client.getPage(url);
                    String xpath;
                    DomText domText;

                    xpath = "//div[@class='cont']/h1/text()";
                    //domText = (DomText) page.getBody().getByXpath(xpath).get(0);
                    HtmlElement body = page.getBody();
                    domText = (DomText) body.getByXPath(xpath).get(0);
                    String title = domText.asText();

                    xpath = "//div[@class='cont']/p[@class='source']/a[1]/text()";
                    //domText = (DomText) page.getBody().getByXpath(xpath).get(0);
                    HtmlElement body2 = page.getBody();
                    domText = (DomText) body2.getByXPath(xpath).get(0);
                    String dynasty = domText.asText();

                    xpath = "//div[@class='cont']/p[@class='source']/a[2]/text()";
                    //domText =(DomText)page.getBody().getByXpath(xpath).get(0);
                    HtmlElement body3 = page.getBody();
                    domText = (DomText) body3.getByXPath(xpath).get(0);
                    String author = domText.asText();

                    xpath = "//div[@class='cont']/div[@class='contson']";
                    //HtmlElement element = (HtmlElement) page.getBody().getByXpath(xpath).get(0);
                    HtmlElement body4 = page.getBody();
                    HtmlElement element = (HtmlElement) body4.getByXPath(xpath).get(0);
                    String content = element.getTextContent().trim();

                    //
                    //计算SHA-256
                    String s = title + content;
                    messageDigest.update(s.getBytes("UTF-8"));
                    byte[] result = messageDigest.digest();
                    StringBuilder sha256 = new StringBuilder();
                    for (byte b : result) {
                        sha256.append(String.format("%02x", b));
                    }


                    //计算分词
                    List<Term> termList = new ArrayList<>();
                    termList.addAll(NlpAnalysis.parse(title).getTerms());
                    termList.addAll(NlpAnalysis.parse(content).getTerms());
                    List<String> words = new ArrayList<>();
                    for (Term term : termList) {
                        if (term.getNatureStr().equals("w")) {
                            continue;
                        }

                        if (term.getNatureStr().equals("null")) {
                            continue;
                        }

                        if (term.getRealName().length() < 2) {
                            continue;
                        }

                        words.add(term.getRealName());
                    }
                    String insertWords = String.join(",", words);
                    try (Connection connection = dataSource.getConnection()) {

                        String sql = "INSERT INTO tangshi " +
                                "(sha256, dynasty, title, author, " +
                                "content, words) " +
                                "VALUES (?, ?, ?, ?, ?, ?)";//占位符

                        try (PreparedStatement statement = connection.prepareStatement(sql)) {

                            statement.setString(1, sha256.toString());
                            statement.setString(2, dynasty);
                            statement.setString(3, title);
                            statement.setString(4, author);
                            statement.setString(5, content);
                            statement.setString(6, insertWords);
                            com.mysql.jdbc.PreparedStatement mysqlStatement = (com.mysql.jdbc.PreparedStatement)statement;
                            System.out.println(mysqlStatement.asSql());    //会每次都打印
                            statement.executeUpdate();
                            //System.out.println(title+"插入成功");


                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                } catch (SQLException e) {
                    if (!e.getMessage().contains("Duplicate entry")) {//如果有则重复插入了，不算错误，过滤掉
                        e.printStackTrace();
                    }
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();//一个任务完成
                }


                }


            }



        public static void main(String[] args) throws Exception {

            ExecutorService pool = Executors.newFixedThreadPool(30);
            WebClient client = new WebClient(BrowserVersion.CHROME);
            client.getOptions().setJavaScriptEnabled(false);//得到选择，期权
            client.getOptions().setCssEnabled(false);

            String baseUrl = "https://so.gushiwen.org";
            String pathUrl = "/gushi/tangshi.aspx";
            List<String> detailUrlList = new ArrayList<>();


            //列表页的解析
            {
                String url = baseUrl + pathUrl;
                HtmlPage page = client.getPage(url);
                List<HtmlElement> divs = page.getBody().getElementsByAttribute("div", "class", "typecont");
                for (HtmlElement div : divs) {
                    List<HtmlElement> as = div.getElementsByTagName("a");
                    for (HtmlElement a : as) {
                        String detailUrl = a.getAttribute("href");//详情
                        detailUrlList.add(baseUrl + detailUrl);//列表页+一首诗  的url
                    }
                }
            }
            MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();

            dataSource.setServerName("127.0.0.1");//是类下的方法
            dataSource.setPort(3306);
            dataSource.setUser("root");
            dataSource.setPassword("12345");
            dataSource.setDatabaseName("tangshi");
            dataSource.setUseSSL(false);
            dataSource.setCharacterEncoding("UTF8");
           // MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            System.out.println("一共有" + detailUrlList.size() + "首诗");
            CountDownLatch countDownLatch = new CountDownLatch(detailUrlList.size());

            //详情页的请求和解析
            for (String url : detailUrlList) {

                pool.execute(new Job(url,dataSource,countDownLatch));
            }
            countDownLatch.await();
            pool.shutdown();

            //等待所有诗都下载结束
//            while (successCount.get() + failureCount.get() < detailUrlList.size()) {
//                System.out.printf("一共%d首诗,成功%d,失败%d\r", detailUrlList.size(), successCount.get(), failureCount.get());
//                TimeUnit.SECONDS.sleep(1);//一秒钟打印一次
//            }
//            //countDownLatch.await();
//            System.out.println();
//            System.out.println("全部下载成功");
//           countDownLatch.await();
//
//        }
    }

}
