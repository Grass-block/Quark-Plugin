// 选择要上传的文件
var fileInput = document.getElementById('fileInput'); // 通过HTML元素获取文件输入框

// 创建一个FormData对象来包装要上传的文件
var formData = new FormData();
formData.append('file', fileInput.files[0]); // 'file' 是后端接受文件的字段名
var xhr = new XMLHttpRequest();

// 配置请求
xhr.open('POST', 'https://example.com/upload', true); // 修改为你的上传URL
xhr.onload = function() {
    if (xhr.status === 200) {
        // 上传成功，执行你的处理代码
        console.log('文件上传成功');
    } else {
        // 上传失败，执行错误处理代码
        console.log('文件上传失败');
    }
};
xhr.send(formData);