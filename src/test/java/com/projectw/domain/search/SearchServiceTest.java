package com.projectw.domain.search;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.projectw.domain.category.DistrictCategory;
import com.projectw.domain.category.HierarchicalCategory;
import com.projectw.domain.category.HierarchicalCategoryUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class SearchServiceTest {

    @Test
    public void test() throws Exception {
        // given
        String inputFilePath = "C:\\Users\\jjho9\\OneDrive\\바탕 화면\\project-w\\src\\main\\resources\\fooddata.csv";
        String outputFilePath = "C:\\Users\\jjho9\\OneDrive\\바탕 화면\\project-w\\src\\main\\resources\\output2.csv";
        List<HierarchicalCategory> c = HierarchicalCategoryUtils.getCategoriesByDepth(DistrictCategory.class, 2);
        try (FileReader fileReader = new FileReader(inputFilePath, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath,  StandardCharsets.UTF_8))) {
            // CSVParser에 quote character를 설정
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(',')
                    .withQuoteChar('"') // 인용 부호를 "로 설정
                    .build();

            // CSVReader에 parser 설정
            CSVReader csvReader = new CSVReaderBuilder(fileReader)
                    .withCSVParser(parser)
                    .build();



            // 한 줄씩 읽어서 처리
            String[] headerLine = csvReader.readNext();
            if (headerLine != null) {
                // 기존 헤더 + 새로운 컬럼명 추가
                writer.write(String.join(",", headerLine) + ",districtCategory");
                writer.newLine();
            }

            // 3번 주소
            String[] record;
            while ((record = csvReader.readNext()) != null) {
                DistrictCategory result = null;
                try {
                    String si = record[3].split("\\s")[0];
                    String gu = record[3].split("\\s")[1];
                    List<String> sss = new ArrayList<>();
                    for(var i : c){
                        sss.clear();
                        String name = i.getName();
                        String s = i.getParent().getName();
                        if(si.equals("광주광역시")) {
                            si = "광주";
                        } else if(si.equals("서울특별시")) {
                            si = "서울";
                        } else if(si.equals("울산광역시")) {
                            si = "울산";
                        } else if(si.equals("강원특별자치도")) {
                            si= "강원도";
                        } else if(si.equals("부산광역시")) {
                            si = "부산";
                        } else if(si.equals("제주특별자치도")) {
                            si = "제주도";
                        }else if(si.equals("대전광역시")) {
                            si = "대전";
                        }else if(si.equals("인천광역시")) {
                            si = "인천";
                        }else if(si.equals("세종특별자치시")) {
                            si = "세종";
                        }

                        if(si.contains(s) && gu.contains(name)) {
                            result = (DistrictCategory) i;
                            break;
                        }
                    }

                    if(si.equals("세종")) result = DistrictCategory.SEJONG;
                    if(result == null) {
                        for(var i : c){
                            String s = i.getName();

                            if(si.contains(s)) {
                                result = (DistrictCategory) i;
                                break;
                            }
                        }
                    }
                    record[3] = "\"" + record[3] + "\"";
                } catch (Exception e) {
                    record[3] = "\"\"";
                } finally {
                    writer.write(String.join(",", record) + "," + (result == null ? "" : result.getPath()));
                    writer.newLine();
                }
            }
            System.out.println("검사완료");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}