Lưu ý, fxml có những <!-- ... --> để bạn dễ đọc
và nút chắc chắn chưa được gắn với controller, chỉ phục vụ mục đích làm đẹp :((
<pre>
1, Điểm chung
mỗi file fxml được triển khai chung theo mô hình chung:
VBox ( chứa thông tin cửa sổ và khởi tạo xmlns )
 |_ Scroll Pane ( Khung hình giúp kéo lên xuống )
          |_ VBox (nhắm sắp xếp những phần tử con theo chiều thẳng đứng trong scroll pane)
               |_ HBox Logo
               |_ HBox Banner
                     |_ Image Holder (Hutao)
                     |_ nút Trang chủ (VBox)
                     |_ nút Hồ sơ (VBox)
<pre>          
2, Trang chủ:
VBox ( chứa thông tin cửa sổ và khởi tạo xmlns )
 |_ Scroll Pane ( Khung hình giúp kéo lên xuống )
         |_ ...
         |
         |_VBox (nhắm sắp xếp những phần tử con theo chiều thẳng đứng trong scroll pane)
            |_ image holder( dự kiến chiếu hình ảnh của sản phẩm đăng trừng bày )
            |_ VBox (khung NỀN chứa những sản phẩm đang được đấu giá (Ongoing Bind), dự kiến thay thế bằng scroll pane)
                |_Label Text + Separator ( Tên mục lục )
                |_HBox (khung CHÍNH chứa sản phẩm đang đấu giá) (mỗi VBox con tham chiếu đến 1 sản phẩm)
                    |_VBox(thông tin sản phẩm: Ảnh và tên được hiện ở VBox)
                    |   |_ImageView(Image holder)
                    |   |_Label(Name holder)
                    |
                  ...
            |_ VBox (khung NỀN chứa những sản phẩm sắp được đấu giá (Ongoing Bind), dự kiến thay thế bằng scroll pane cấu trúc tương tự)




|_ VBox( chứa thông tin aboutus)
         
<pre>
