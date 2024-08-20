import java.sql.*;
import java.util.Scanner;

public class MiniProj3  {
	private static Connection conn;
    private static Scanner scanner = new Scanner(System.in);

    
    	 public static void main(String[] args) throws Exception {
    		
    	      try {
    	         //JDBC Driver 등록
    	         Class.forName("oracle.jdbc.OracleDriver");
    	         
    	         //연결하기
    	         conn = DriverManager.getConnection(
    	            "jdbc:oracle:thin:@localhost:1521/xe", 
    	            "miniproj1", 
    	            "1004"
    	         );  
    	         
    	         MiniProj3 proj = new MiniProj3();
    	         
            while (true) {
            	System.out.println("-----------------------------------------------------------------");
            	System.out.println("미니 프로젝트 1차");
            	System.out.println("-----------------------------------------------------------------");
                System.out.println("1. 회원 가입");
                System.out.println("2. 로그인");
                System.out.println("3. 아이디 찾기");
                System.out.println("4. 비밀번호 초기화");
                System.out.println("5. 종료");
                System.out.println();
                System.out.print("원하는 기능? : ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // 버퍼 정리

                switch (choice) {
                    case 1:
                        proj.signUp();
                        break;
                    case 2:
                    	proj.login();
                        break;
                    case 3:
                    	proj.findId();
                        break;
                    case 4:
                    	proj.resetPassword();
                        break;
                    case 5:
                        System.out.println("프로그램을 종료합니다.");
                        return;
                    default:
                        System.out.println("올바른 옵션을 선택하세요.");
                }
            }
           
        } catch (SQLException e) {
            e.printStackTrace();
            
        }
    	     
  	 }
    //로그인 함수 등록	 
    public void signUp() {
        try {
        	System.out.println("---------------------------------------------------------");
            System.out.print("아이디: ");
            String id = scanner.nextLine();
            if (isIdExists(id)) {
                System.out.println("아이디가 중복됩니다.");
                return;
            }

            System.out.print("비번: ");
            String password = scanner.nextLine();
            System.out.print("이름: ");
            String name = scanner.nextLine();
            System.out.print("전화번호: ");
            String phone = scanner.nextLine();
            System.out.print("주소: ");
            String address = scanner.nextLine();
            System.out.print("성별: ");
            String gender = scanner.nextLine();

            String sql = "INSERT INTO Members (id, password, name, phone, address, gender) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, password);
            pstmt.setString(3, name);
            pstmt.setString(4, phone);
            pstmt.setString(5, address);
            pstmt.setString(6, gender);
            pstmt.executeUpdate();

            System.out.println("가입을 축하합니다.");
            System.out.println("---------------------------------------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isIdExists(String id) throws SQLException {
        String sql = "SELECT id FROM Members WHERE id = ? AND deleted = 0";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        ResultSet rs = pstmt.executeQuery();
        return rs.next();
    }

    public void login() {
        try {
            System.out.print("아이디: ");
            String id = scanner.nextLine();
            System.out.print("비밀번호: ");
            String password = scanner.nextLine();

            String sql = "SELECT * FROM Members WHERE id = ? AND password = ? AND deleted = 0";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("로그인 성공!");

                // 로그인 시간 기록
                recordLogin(id);

                while (true) {
                    System.out.println("1. 나의정보확인");
                    System.out.println("2. 게시물 목록");
                    System.out.println("3. 게시물 작성");
                    System.out.println("4. 회원 목록(관리자인경우)");
                    System.out.println("5. 로그아웃");
                    System.out.println("6. 종료");
                    System.out.print("원하는 기능? ");
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    if (choice == 1) {
                        viewMyInfo(id);
                    } else if (choice == 2) {
                        viewPostList(id);
                    } else if (choice == 3) {
                        createPost(id); 
                    } else if (choice == 4) {
                        if (isAdmin(id)) {
                            viewMemberList();
                        } else {
                            System.out.println("권한이 없습니다.");
                        }
                    } else if (choice == 5) {
                        recordLogout(id);
                        System.out.println("로그아웃 되었습니다.");
                        break;
                    } else if (choice == 6) {
                    	System.out.println("프로그램이 종료되었습니다.");
                        System.exit(0);  
                    } else {
                        System.out.println("올바른 옵션을 선택하세요.");
                    }
                }
            } else {
                System.out.println("로그인 실패. 아이디 또는 비밀번호를 확인하세요.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordLogin(String id) throws SQLException {
        String sql = "UPDATE Members SET last_login = SYSDATE WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        pstmt.executeUpdate();

        sql = "INSERT INTO LoginHistory (member_id, login_time) VALUES (?, SYSDATE)";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        pstmt.executeUpdate();
    }

    public void recordLogout(String id) throws SQLException {
        String sql = "UPDATE Members SET last_logout = SYSDATE WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        pstmt.executeUpdate();

        sql = "UPDATE LoginHistory SET logout_time = SYSDATE WHERE member_id = ? AND logout_time IS NULL";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        pstmt.executeUpdate();
    }

    public void viewMyInfo(String id) throws SQLException {
        String sql = "SELECT * FROM Members WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            System.out.println("아이디: " + rs.getString("id"));
            System.out.println("이름: " + rs.getString("name"));
            System.out.println("전화번호: " + rs.getString("phone"));
            System.out.println("주소: " + rs.getString("address"));
            System.out.println("성별: " + rs.getString("gender"));
            System.out.println("마지막 로그인: " + rs.getTimestamp("last_login"));
            System.out.println("마지막 로그아웃: " + rs.getTimestamp("last_logout"));
        } else {
            System.out.println("사용자 정보를 찾을 수 없습니다.");
        }
    }

    public void viewPostList(String userId) {
        try {
            int page = 1;
            while (true) {
                displayPosts(page);

                System.out.print("메뉴 (상세보기 번호, P - 이전 페이지, N - 다음 페이지, 0 - 종료): ");
                String choice = scanner.nextLine();

                if (choice.equalsIgnoreCase("P")) {
                    if (page > 1) {
                        page--;
                    } else {
                        System.out.println("이전 페이지가 없습니다.");
                    }
                } else if (choice.equalsIgnoreCase("N")) {
                    page++;
                } else if (choice.equals("0")) {
                    break;
                } else {
                    try {
                        int postId = Integer.parseInt(choice);
                        viewPostDetail(postId, userId);
                    } catch (NumberFormatException e) {
                        System.out.println("올바른 번호를 입력하세요.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void displayPosts(int page) throws SQLException {
        int offset = (page - 1) * 10;
        int limit = 10;
        String sql = "SELECT * FROM ("
                   + "SELECT post_id, author_id, title, content, views, created_at, ROWNUM rnum "
                   + "FROM Posts "
                   + "ORDER BY created_at DESC) "
                   + "WHERE rnum BETWEEN ? AND ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, offset + 1);
        pstmt.setInt(2, offset + limit);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            int postId = rs.getInt("post_id");
            String authorId = rs.getString("author_id");
            String title = rs.getString("title");
            String content = rs.getString("content");
            int views = rs.getInt("views");
            Timestamp createdAt = rs.getTimestamp("created_at");

            String displayTime = (System.currentTimeMillis() - createdAt.getTime() <= 86400000) ?
                    createdAt.toLocalDateTime().toLocalTime().toString() :
                    createdAt.toLocalDateTime().toLocalDate().toString();

            System.out.printf("게시물 번호 : %d |작성자 ID : %s | 제목 : %s |내용 : %s | 조회수 : %d | 작성일자 : %s\n", postId, authorId, title, content, views, displayTime);
        }
        System.out.println("<현재 페이지: " + page+">");
    }

    public void viewPostDetail(int postId, String userId) throws SQLException {
        String sql = "SELECT * FROM Posts WHERE post_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, postId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            System.out.println("제목: " + rs.getString("title"));
            System.out.println("내용: " + rs.getString("content"));
            System.out.println("작성자: " + rs.getString("author_id"));
            System.out.println("작성일: " + rs.getTimestamp("created_at"));

            // 조회수 증가
            updatePostViews(postId);

            if (rs.getString("author_id").equals(userId)) {
                System.out.print("1. 수정 2. 삭제 0. 종료: ");
                int choice = scanner.nextInt();
                scanner.nextLine();
                if (choice == 1) {
                    updatePost(postId);
                } else if (choice == 2) {
                    deletePost(postId);
                }
            }
        } else {
            System.out.println("게시물을 찾을 수 없습니다.");
        }
    }

    public void updatePostViews(int postId) throws SQLException {
        String sql = "UPDATE Posts SET views = views + 1 WHERE post_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, postId);
        pstmt.executeUpdate();
    }

    public void updatePost(int postId) throws SQLException {
        System.out.print("수정할 제목: ");
        String title = scanner.nextLine();
        System.out.print("수정할 내용: ");
        String content = scanner.nextLine();
        System.out.print("비밀번호 확인: ");
        String password = scanner.nextLine();

        String sql = "SELECT password FROM Posts WHERE post_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, postId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next() && rs.getString("password").equals(password)) {
            sql = "UPDATE Posts SET title = ?, content = ? WHERE post_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setInt(3, postId);
            pstmt.executeUpdate();
            System.out.println("게시물이 수정되었습니다.");
        } else {
            System.out.println("비밀번호가 일치하지 않습니다.");
        }
    }

    public void deletePost(int postId) throws SQLException {
        System.out.print("비밀번호 확인: ");
        String password = scanner.nextLine();

        String sql = "SELECT password FROM Posts WHERE post_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, postId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next() && rs.getString("password").equals(password)) {
            sql = "DELETE FROM Posts WHERE post_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, postId);
            pstmt.executeUpdate();
            System.out.println("게시물이 삭제되었습니다.");
        } else {
            System.out.println("비밀번호가 일치하지 않습니다.");
        }
    }
    
    public void createPost(String userId) throws SQLException {
        System.out.println("게시물 작성");
        System.out.print("제목: ");
        String title = scanner.nextLine();
        System.out.print("내용: ");
        String content = scanner.nextLine();
        System.out.print("게시물 비밀번호: ");
        String password = scanner.nextLine();

        String sql = "INSERT INTO Posts (author_id, title, content, password, created_at, views) VALUES (?, ?, ?, ?, SYSDATE, 0)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userId);
        pstmt.setString(2, title);
        pstmt.setString(3, content);
        pstmt.setString(4, password);
        pstmt.executeUpdate();

        System.out.println("게시물이 작성되었습니다.");
    }


    public void findId() {
        try {
            System.out.println("---------------------------------------------------------");
            System.out.print("가입된 회원님의 이름: ");
            String name = scanner.nextLine();

            // 사용자 이름으로 ID를 조회합니다.
            String sql = "SELECT id FROM Members WHERE name = ? AND deleted = 0";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                
                do {
                    System.out.println("가입된 아이디 : "+rs.getString("id"));
                } while (rs.next());
            } else {
                System.out.println("해당 이름으로 가입된 아이디가 없습니다.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void resetPassword() {
        try {
            System.out.println("---------------------------------------------------------");
            System.out.print("아이디: ");
            String id = scanner.nextLine();

            // 회원의 현재 비밀번호를 확인합니다.
            System.out.print("현재 비밀번호: ");
            String currentPassword = scanner.nextLine();

            // 사용자가 입력한 아이디와 현재 비밀번호가 데이터베이스와 일치하는지 확인합니다.
            String sql = "SELECT password FROM Members WHERE id = ? AND password = ? AND deleted = 0";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, currentPassword);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // 현재 비밀번호가 일치하는 경우 새 비밀번호를 입력받습니다.
                System.out.print("새 비밀번호: ");
                String newPassword = scanner.nextLine();
                
                // 새 비밀번호로 업데이트합니다.
                sql = "UPDATE Members SET password = ? WHERE id = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, newPassword);
                pstmt.setString(2, id);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("비밀번호가 성공적으로 초기화되었습니다.");
                } else {
                    System.out.println("비밀번호 초기화에 실패했습니다.");
                }
            } else {
                System.out.println("아이디 또는 현재 비밀번호가 올바르지 않습니다.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void viewMemberList() {
        // 관리자인지 확인
        if (!isAdmin("admin")) {
            System.out.println("관리자만 접근할 수 있습니다.");
            return;
        }

        try {
            // SQL 쿼리 작성
            String sql = "SELECT id, name, phone, address, gender, last_login, last_logout FROM Members WHERE deleted = 0";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("---------------------------------------------------------");
            System.out.println("회원 목록:");
            System.out.println("---------------------------------------------------------");

            // 결과를 반복문으로 출력
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String phone = rs.getString("phone");
                String address = rs.getString("address");
                String gender = rs.getString("gender");
                Timestamp lastLogin = rs.getTimestamp("last_login");
                Timestamp lastLogout = rs.getTimestamp("last_logout");

                // 회원 정보를 출력
                System.out.printf("아이디: %s | 이름: %s | 전화번호: %s | 주소: %s | 성별: %s | 마지막 로그인: %s | 마지막 로그아웃: %s\n",
                        id, name, phone, address, gender, lastLogin, lastLogout);
            }
            
            System.out.println("---------------------------------------------------------");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isAdmin(String id) {
        // 관리자인지 확인하는 로직 구현 (예: 특정 id가 관리자인 경우 true 반환)
        return "admin".equals(id); // 예시로 'admin'을 관리자로 설정
    }
}