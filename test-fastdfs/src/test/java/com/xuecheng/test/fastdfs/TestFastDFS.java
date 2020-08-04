package com.xuecheng.test.fastdfs;

import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {

    @Test
    public void testUpload(){
        //加载fastdfs-client.properties配置文件
        try {
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义TrackerClient，用于请求TrackerServer
            TrackerClient trackerClient=new TrackerClient();
            //连接tracker
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取Stroage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建stroageClient
            StorageClient1 storageClient1= new StorageClient1(trackerServer,storeStorage);
            //向stroage服务器上传文件
            //本地文件的路径
            String path="D:/mylog.log";
            //上传成功后拿到文件Id
            String fileId = storageClient1.upload_appender_file1(path, "log", null);
            System.out.println(fileId);
            //group1/M00/00/00/wKhdgF3fWCKEI4PBAAAAAIipfIQ714.log

        }  catch (Exception e) {
            e.printStackTrace();
        }
    }
    //下载文件
    @Test
    public void testDownload(){
        try {
            //加载fastdfs-client.properties配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义TrackerClient，用于请求TrackerServer
            TrackerClient trackerClient = new TrackerClient();
            //连接tracker
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取Stroage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建stroageClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storeStorage);
            //下载文件
            //文件id
            String fileId = "group1/M00/00/00/wKhdgF3fWCKEI4PBAAAAAIipfIQ714.log";
            byte[] bytes = storageClient1.download_file1(fileId);
            //使用输出流保存文件
            FileOutputStream fileOutputStream = new FileOutputStream(new File("E:/mylog.log"));
            fileOutputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void sort(){
        int [] arr={9,2,5,8,3,7,4,6,1};
        for(int i=0;i<arr.length-1;i++) {
            for (int j = 0; j <arr.length-1-i; j++) {
                if (arr[j] > arr[j + 1]) {
                    int swap = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = swap;
                }
            }
        }
        //输出
        for(int i=0;i<arr.length;i++)
        {
            System.out.println(arr[i]);
        }

    }

    @Test
    public void sort2(){
        int [] arr={9,2,5,8,3,7,4,6,1};
        for(int i=0;i<2;i++)
        {
            for(int j=arr.length-2;j>=i;j--)
            {
                if (arr[j] > arr[j + 1]) {
                    int swap = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = swap;
                }
            }
        }
        //输出
        for(int i=0;i<arr.length;i++)
        {
            System.out.println(arr[i]);
        }
    }
    @Test
    //快排
    public void quicksort()
    {
         int [] arr={9,2,5,8,3,7,4,6,1};
         int low=0;
         int high=8;
         Qsort(arr,low,high);
        //输出
        for(int i=0;i<arr.length;i++)
        {
            System.out.println(arr[i]);
        }


    }
    public  void Qsort(int arr[],int low,int high)
    {
        int point;
        if(low<high)
        {
            point=sort(arr,low,high);
            Qsort(arr,low,point-1);
            Qsort(arr,point+1,high);
        }

    }
    public int sort(int arr[],int low,int high){
        int p=arr[low];
        while (low<high)
        {
            while (low<high && arr[high]>=p)
                high--;
            swap(arr,low,high);
            while(low<high && arr[low]<=p)
                low++;
            swap(arr,low,high);
        }
        return low;
    }
    public void swap(int arr[],int low,int high)
    {
        int m=arr[low];
        arr[low]=arr[high];
        arr[high]=m;
    }
}
