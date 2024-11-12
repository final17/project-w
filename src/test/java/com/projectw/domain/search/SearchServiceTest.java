package com.projectw.domain.search;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class SearchServiceTest {
//    @Test
//    void autoComplete_예외_발생시_빈_리스트_반환() throws IOException {
//        // given
//        given(elasticsearchClient.search(any(), eq(StoreDoc.class))).thenThrow(new IOException("Elasticsearch exception"));
//
//        // given
//        KeywordSearchResponse.AutoComplete response = searchService.autoComplete(keyword);
//
//        // then
//        assertThat(response.getKeyword()).isEqualTo(keyword);
//        assertThat(response.getStores()).isEmpty();
//    }
//
//    @Test
//    void intergratedSearch_성공_응답() throws IOException {
//        // given
//        KeywordSearchRequest.Search searchRequest = new KeywordSearchRequest.Search(keyword, null, 30);
//        SearchResponse<StoreDoc> mockResponse = mock(SearchResponse.class);
//        Hit<StoreDoc> hit = new Hit<>();
//        StoreDoc storeDoc = new StoreDoc();
//        hit.setSource(storeDoc);
//
//        given(mockResponse.hits().hits()).willReturn(List.of(hit));
//        given(elasticsearchClient.search(any(), eq(StoreDoc.class))).willReturn(mockResponse);
//
//        // given
//        KeywordSearchResponse.Search response = searchService.intergratedSearch(searchRequest);
//
//        // then
//        assertThat(response.getKeyword()).isEqualTo(keyword);
//        assertThat(response.getStores()).hasSize(1);
//        assertThat(response.getStores().get(0)).isEqualTo(storeDoc);
//    }
//
//    @Test
//    void intergratedSearch_예외_발생시_빈_리스트_반환() throws IOException {
//        // given
//        KeywordSearchRequest.Search searchRequest = new KeywordSearchRequest.Search(keyword, null, 30);
//        given(elasticsearchClient.search(any(), eq(StoreDoc.class))).thenThrow(new IOException("Elasticsearch exception"));
//
//        // given
//        KeywordSearchResponse.Search response = searchService.intergratedSearch(searchRequest);
//
//        // then
//        assertThat(response.getKeyword()).isEqualTo(keyword);
//        assertThat(response.getStores()).isEmpty();
//    }


//    @Test
//    public void test() throws Exception {
//        // given
//        String inputFilePath = "C:\\Users\\jjho9\\OneDrive\\바탕 화면\\project-w\\src\\main\\resources\\fooddata_es.csv";
//        String outputFilePath = "C:\\Users\\jjho9\\OneDrive\\바탕 화면\\project-w\\src\\main\\resources\\fooddata2.csv";
//        List<HierarchicalCategory> c = HierarchicalCategoryUtils.getCategoriesByDepth(DistrictCategory.class, 2);
//        try (FileReader fileReader = new FileReader(inputFilePath, StandardCharsets.UTF_8);
//             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath,  StandardCharsets.UTF_8))) {
//            // CSVParser에 quote character를 설정
//            CSVParser parser = new CSVParserBuilder()
//                    .withSeparator(',')
//                    .withQuoteChar('"') // 인용 부호를 "로 설정
//                    .build();
//
//            // CSVReader에 parser 설정
//            CSVReader csvReader = new CSVReaderBuilder(fileReader)
//                    .withCSVParser(parser)
//                    .build();
//
//
//
//            // 한 줄씩 읽어서 처리
//            String[] headerLine = csvReader.readNext();
//            if (headerLine != null) {
//                // 기존 헤더 + 새로운 컬럼명 추가
//                writer.write(String.join(",", headerLine));
//                writer.newLine();
//            }
//
//            // 3번 주소
//            String[] record;
//            while ((record = csvReader.readNext()) != null) {
//                DistrictCategory result = null;
//                try {
//                    String si = record[3].split("\\s")[0];
//                    String gu = record[3].split("\\s")[1];
//                    List<String> sss = new ArrayList<>();
//                    for(var i : c){
//                        sss.clear();
//                        String name = i.getName();
//                        String s = i.getParent().getName();
//                        if(si.equals("광주광역시")) {
//                            si = "광주";
//                        } else if(si.equals("서울특별시")) {
//                            si = "서울";
//                        } else if(si.equals("울산광역시")) {
//                            si = "울산";
//                        } else if(si.equals("강원특별자치도")) {
//                            si= "강원도";
//                        } else if(si.equals("부산광역시")) {
//                            si = "부산";
//                        } else if(si.equals("제주특별자치도")) {
//                            si = "제주도";
//                        }else if(si.equals("대전광역시")) {
//                            si = "대전";
//                        }else if(si.equals("인천광역시")) {
//                            si = "인천";
//                        }else if(si.equals("세종특별자치시")) {
//                            si = "세종";
//                        }
//
//                        if(si.contains(s) && gu.contains(name)) {
//                            result = (DistrictCategory) i;
//                            break;
//                        }
//                    }
//
//                    if(si.equals("세종")) result = DistrictCategory.SEJONG;
//                    if(result == null) {
//                        for(var i : c){
//                            String s = i.getName();
//
//                            if(si.contains(s)) {
//                                result = (DistrictCategory) i;
//                                break;
//                            }
//                        }
//                    }
//                    record[1] = LocalDateTime.now().minusDays(1L).toString();
//                    record[2] = LocalDateTime.now().minusDays(1L).toString();
//                } catch (Exception e) {
//                } finally {
//                    writer.write(String.join(",", record) + (result == null ? DistrictCategory.SEOUL.name(): result.name()));
//                    writer.newLine();
//                }
//            }
//            System.out.println("검사완료");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    @Test
//    public void test3333() throws Exception {
//        // given
//        String inputFilePath = "C:\\Users\\jjho9\\OneDrive\\바탕 화면\\project-w\\src\\main\\resources\\fooddata3.csv";
//        String outputFilePath = "C:\\Users\\jjho9\\OneDrive\\바탕 화면\\project-w\\src\\main\\resources\\fooddata_es.csv";
//        List<HierarchicalCategory> c = HierarchicalCategoryUtils.getCategoriesByDepth(DistrictCategory.class, 2);
//        try (FileReader fileReader = new FileReader(inputFilePath, StandardCharsets.UTF_8);
//             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath,  StandardCharsets.UTF_8))) {
//            // CSVParser에 quote character를 설정
//            CSVParser parser = new CSVParserBuilder()
//                    .withSeparator(',')
//                    .build();
//
//            // CSVReader에 parser 설정
//            CSVReader csvReader = new CSVReaderBuilder(fileReader)
//                    .withCSVParser(parser)
//                    .build();
//
//
//
//            // 한 줄씩 읽어서 처리
//            String[] headerLine = csvReader.readNext();
//            if (headerLine != null) {
//                // 기존 헤더 + 새로운 컬럼명 추가
//                writer.write(String.join(",", headerLine));
//                writer.newLine();
//            }
//
//            // 3번 주소
//            String[] record;
//            DistrictCategory[] values = DistrictCategory.values();
//            int cnt = 0;
//            while ((record = csvReader.readNext()) != null) {
////                record[1] = LocalDateTime.now().minusDays(1L).toString();
////                record[2] = LocalDateTime.now().minusDays(1L).toString();
////                writer.write(String.join(",", record));
////                writer.newLine();
//                DistrictCategory result = null;
//                int last = record.length - 1;
//                String s = record[last];
//
//                for (DistrictCategory value : values) {
//                    if (s.equals(value.name())) {
//                        result = value;
//                        break;
//                    }
//                }
//
//                if(result != null) {
//                    String name = result.getPath();
//                    record[last] = name;
//                }
//                writer.write(String.join(",", record));
//                writer.newLine();
//                ++cnt;
//                System.out.println("cnt = " + cnt);
//            }
//            System.out.println("검사완료");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void test22() throws Exception {
//        // given
//        String inputFilePath = "C:\\Users\\jjho9\\OneDrive\\바탕 화면\\project-w\\src\\main\\resources\\fooddata_db.csv";
//        String outputFilePath = "C:\\Users\\jjho9\\OneDrive\\바탕 화면\\project-w\\src\\main\\resources\\fooddata_db.csv";
//        List<HierarchicalCategory> c = HierarchicalCategoryUtils.getCategoriesByDepth(DistrictCategory.class, 2);
//        try (FileReader fileReader = new FileReader(inputFilePath, StandardCharsets.UTF_8);
//             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath,  StandardCharsets.UTF_8))) {
//            // CSVParser에 quote character를 설정
//            CSVParser parser = new CSVParserBuilder()
//                    .withSeparator(',')
//                    .withQuoteChar('"') // 인용 부호를 "로 설정
//                    .build();
//
//            // CSVReader에 parser 설정
//            CSVReader csvReader = new CSVReaderBuilder(fileReader)
//                    .withCSVParser(parser)
//                    .build();
//
//
//            // 한 줄씩 읽어서 처리
//            String[] headerLine = csvReader.readNext();
//            if (headerLine != null) {
//                // 기존 헤더 + 새로운 컬럼명 추가
//                writer.write(String.join(",", headerLine));
//                writer.newLine();
//            }
//
//            int size = headerLine.length;
//            // 3번 주소
//            String[] record;
//            DistrictCategory[] values = DistrictCategory.values();
//            while ((record = csvReader.readNext()) != null) {
////                DistrictCategory result = null;
////                int last = record.length - 1;
////                String s = record[last];
//
////                for (DistrictCategory value : values) {
////                    if (s.equals(value.name())) {
////                        result = value;
////                        break;
////                    }
////                }
////
////                if(result != null) {
////                    String name = result.getPath();
////                    record[last] = name;
////                }
//                // 4, 10
//                String close = record[4];
//                int i = close.indexOf(':');
//                if(close.substring(0, i).length() != 2){
//                    record[4] = "0"+record[4];
//                }
//                String open = record[10];
//                i = open.indexOf(':');
//                if(open.substring(0, i).length() != 2){
//                    record[10] = "0"+record[10];
//                }
//
//                String lo = record[9];
//                i = lo.indexOf(':');
//                if(lo.substring(0, i).length() != 2){
//                    record[9] = "0"+record[9];
//                }
//
//                String turnover = record[15];
//                i = turnover.indexOf(':');
//                if(turnover.substring(0, i).length() != 2){
//                    record[15] = "0"+record[15];
//                }
//                writer.write(String.join(",", record));
//                writer.newLine();
//            }
//            System.out.println("검사완료");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}