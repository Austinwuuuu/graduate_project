package com.example.myproject;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class db {

    // 資料庫定義
    String mysql_ip = "140.127.220.88";
    int mysql_port = 3306;
    String db_name = "thecrowd";
    String url = "jdbc:mysql://"+mysql_ip+":"+mysql_port+"/"+db_name+"?useSSL=false&characterEncoding=UTF-8"; //不使用SSL，字元編碼UTF-8
    String db_user = "fuckyouharry";
    String db_password = "MEWatRn9";

    java.sql.Connection con = null;
    public void run() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Log.v("DB","加載驅動成功");
        }catch( ClassNotFoundException e) {
            Log.e("DB","加載驅動失敗");
            return;
        }

        // 連接資料庫
        try {
            con = java.sql.DriverManager.getConnection(url,db_user,db_password);
            Log.v("DB","遠端連接成功");
        }catch(SQLException e) {
            Log.e("DB","遠端連接失敗");
            Log.e("DB", e.toString());
        }
    }

    public ArrayList<String> getExname() { // 取得展區名稱
        ArrayList<String> data = new ArrayList<>();
        try {
            Connection conn = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT exName FROM exhibition";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next())
            {
                String exName = rs.getString("exName");
                data.add(exName);
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public ArrayList<String> getPreference(String exName) { // 取得偏好值
        ArrayList<String> data = new ArrayList<>();
        int exId = 0; // 展區編號
        try {
            Connection conn = DriverManager.getConnection(url, db_user, db_password);

            // 取得使用者所選展區之編號
            String select_exId = "SELECT exId FROM exhibition WHERE exName = ?";
            PreparedStatement ps1 = conn.prepareStatement(select_exId);
            ps1.setString(1, exName);
            ResultSet rs1 = ps1.executeQuery();
            while(rs1.next()){
                exId = rs1.getInt("exId");
            }

            // 取出對應該展區的分類
            String select_category = "SELECT DISTINCT category FROM building WHERE belongExhibitionId = ?";
            PreparedStatement ps2 = conn.prepareStatement(select_category);
            ps2.setInt(1, exId);
            ResultSet rs2 = ps2.executeQuery();

            while (rs2.next())
            {
                String category = rs2.getString("category");
                if (!category.equals("")) {
                    data.add(category);
                }
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public String getExLocation(String exName){ // 取得展區中心點經緯度
        String location = null;

        try {
            Connection conn = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT location FROM exhibition WHERE exName = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, exName); // 設定參數值
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                location = rs.getString("location");
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return location;
    }

    public Map<String, ArrayList<Object>> getBuildingLocation(String exName) { // 取得高大展區內建築各經緯度
        ArrayList<Object> arrL; // 放degree和location
        Map<String, ArrayList<Object>> data = new HashMap<>();
        int exId = 0;
        try {
            Connection conn = DriverManager.getConnection(url, db_user, db_password);

            String select_exId = "SELECT exId FROM exhibition WHERE exName = ?";
            PreparedStatement ps1 = conn.prepareStatement(select_exId);
            ps1.setString(1, exName);
            ResultSet rs1 = ps1.executeQuery();
            while(rs1.next()){
                exId = rs1.getInt("exId");
            }

            String select_bLocation  = "SELECT name, degree, location,category FROM building WHERE belongExhibitionId = ?";
            PreparedStatement ps2 = conn.prepareStatement(select_bLocation);
            ps2.setInt(1, exId);
            ResultSet rs2 = ps2.executeQuery();

            while (rs2.next())
            {
                String name = rs2.getString("name");
                int degree = rs2.getInt("degree");
                String location = rs2.getString("location");
                String category = rs2.getString("category");

                arrL = new ArrayList<>();
                arrL.add(degree);
                arrL.add(location);
                arrL.add(category);

                data.put(name, arrL);
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

}
