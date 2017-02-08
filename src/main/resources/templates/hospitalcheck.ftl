<#import "spring.ftl" as spring>
<!DOCTYPE HTML>
<html>
 <head>
   <title>医院验收</title>
   <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
   <link href="/css/bootstrap.min.css" rel="stylesheet" type="text/css" />
   <link rel="stylesheet" type="text/css" href="/css/select2.min.css">
 </head>
 <body>
    <div>
       <h3 class="page-header">趣保险服务演示平台</h3>
    </div>

    <div class="row">
        <form action="" id="params">
            <div class="col-sm-12 col-md-8 col-lg-9">
                
                <div class="col-sm-8 col-md-4 col-lg-3">
                    <div class="form-group">
                        <label class="control-label"><font color=#FF0000>医院号：</font></label>
                        <input type="text" class="form-control" id="hospitalId" name="hospitalId">
                        <#--<select class="form-control selectpicker" name="hospitalId" id="hospitalId" title="医院名称"></select>-->
                    </div>
                </div>
                <div class="col-sm-8 col-md-4 col-lg-2">
                    <div class="form-group">
                        <label class="control-label"><font color=#FF0000>患者姓名：</font></label>
                        <input type="text" class="form-control" id="patientName" name="patientName">
                    </div>
                </div>
                <div class="col-sm-8 col-md-4">
                    <div class="form-group">
                        <label class="control-label">身份证号：</label>
                        <input type="text" class="form-control" id="idCardNo" name="idCardNo">
                    </div>
                </div>
                <div class="col-sm-8 col-md-4 col-lg-2">
                    <div class="form-group">
                        <label class="control-label">住院号：</label>
                        <input type="text" class="form-control" id="hospitalizationNo" name="hospitalizationNo">
                    </div>
                </div>
            
                <div class="col-sm-8 col-md-4 col-lg-2">
                    <div class="form-group">
                        <label class="control-label">&nbsp;</label><br/>
                        <button type="button" class="btn btn-primary" onclick="exportExcel('/hospital/checked')">
                            查询生成报告
                        </button>
                    </div>
                </div>
            </div>
        </form>
    </div>
    
    <div class="modal fade" id="mymodal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">  
        <div class="modal-dialog">  
            <div class="modal-content">  
                <div class="modal-header"></div>  
                <div class="modal-body">  
                    数据处理中，请稍后。。。
                </div>  
                <div class="modal-footer"></div>  
            </div>  
        </div>
    </div> 
     
 </body>
 <script src="/js/jquery.min.js" type="text/javascript"></script>
 <script src="/js/jquery.form.js" type="text/javascript"></script>
 <script src="/js/bootstrap.min.js" type="text/javascript"></script>
 <script>
 
    
    //$(function(){  
         //initSelect("hospitalId","/hospital/map");
    //});
    function exportExcel(url) {
        $('#mymodal').modal('show');
	    document.getElementById('params').action = url;
	    document.getElementById('params').submit();
	    //setTimeout(function(){
	       //$('#mymodal').modal('hide');
	    //},5000);
	    var interval = setInterval(function(){
	         $.ajax({
	            url: "/monitor/progress",
	            type: "POST",
	            processData: false,
	            contentType: false,
	            cache: false,
	            success: function(data) {
                    if(data==true){
                        $('#mymodal').modal('hide');
                        clearInterval(interval);
                    }
	           	}
	        });
	    },1000)
    }
    
    function progress(){
        $.ajax({
	            url: "/monitor/progress",
	            type: "POST",
	            processData: false,
	            contentType: false,
	            cache: false,
	            success: function(data) {
                    if(data==true){
                        $('#mymodal').modal('hide');
                        clearInterval(interval);
                    }
	           	},
	            error: function () {
	            }
	        });
	}
	
	

	
   
 </script>
</html>