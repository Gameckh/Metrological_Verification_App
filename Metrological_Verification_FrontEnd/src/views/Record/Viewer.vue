<template>
  <div class="designer">
     <el-row class="toolbar">
      <el-col :span="3"><el-input v-model="name" placeholder="请输入记录名称"></el-input></el-col>
      <el-col :span="18">
        <el-button-group>
      <el-button type="primary" @click="saveRecord()">保存</el-button>
      </el-button-group>
        <el-button-group>
      <el-button @click="lookReport()">预览报表</el-button>
      <el-button @click="goRcordList()">填报记录</el-button>
      </el-button-group>

      <el-button-group>
        <input type="file" ref="imageFile" :disabled = "disableInsertImage"/>
      <el-button @click="insertImage()" :disabled = "disableInsertImage">插入图片</el-button>

      </el-button-group>
      </el-col>
      </el-row>
      <div>
        <div style="width:48%; float:left">
          <gc-spread-sheets
            :hostStyle="hostStyle"
            @workbookInitialized="workbookInitialized"
          ></gc-spread-sheets>
        </div>
        <div style="width:48%; float:left">
          <embed type="" :src="pdfSource" v-if="pdfSource" style="margin-left:10px" width="100%" height="580px">
        </div>
      </div>
  </div>
</template>

<script>
// @ is an alias to /src

import "@grapecity-software/spread-sheets/styles/gc.spread.sheets.excel2013white.css";
import * as GC from "@grapecity-software/spread-sheets";
import "@grapecity-software/spread-sheets-resources-zh";
import "@grapecity-software/spread-sheets-vue";
import "@grapecity-software/spread-sheets-print"

GC.Spread.Common.CultureManager.culture("zh-cn");
import HttpUtils from '../../utils/httpUtils'
import Gzip from '../../utils/gzip'
import { Notification } from 'element-ui';
import { processCellImage, addCellImage, processLock, processTable } from '../../components/SpreadSheets/common'
GC.Spread.Sheets.LicenseKey = "guanyuanshuju,695962798612895#B1JRKVohEUnRTZ9c7bw2kNFJjb8FnMH56NLFWYi9ERMdlbtFnYlVDUjFlYYJDTo36NIxUORRGUHFmWWdzQwx4SHlHbJhmZzM6YhxWRzAVSyh4TwYDNQF5dr56ZwQnd9JzTHRGZGdFWjJXd6hkaOpEVwknMHxUS9pUVmlUOxwmVzNzcCJzZQN5avRWMzVlesNGTEVkZ0NnWLJ5UjJmc7IHTQlURzsGaycWSuN5NjF7UvZnMwAnWYZkZ85EOTtyQ7EWOMJjTQp5cxITc4REZmlVc4olYGlmMMJTNlJEUHFTOnJlQHRVU7YWViojITJCLiETR7IDM8MkNiojIIJCLzgDM9QjNyYzN0IicfJye#4Xfd5nIVF4SRJiOiMkIsIyNx8idgMlSgQWYlJHcTJiOi8kI1tlOiQmcQJCLiMDNzQTMwAyNwMDM4IDMyIiOiQncDJCLi8ajmDblmz9voL8po19tl7anmLiOiEmTDJCLiUTO8ITM6gTO7IjN9UTO6IiOiQWSiwSfdtlOicGbmJCLlNHbhZmOiI7ckJye0ICbuFkI1pjIEJCLi4TPnRkV8QDMOlXbM34TWFTd9FTM8Mmb95mQUR6Vmx4dzsyQSdzZBdTNp9kRUBXRGB5MLN6MVhGMJNlN4sERQVXWotmYDpXTjNVYxsWRqJEWRVVeLlkWpZWNPNGN9olUlF4UyZVazckWlVnWwhkN4RIK";
// GC.Spread.Sheets.Designer.LicenseKey = "guanyuanshuju,457244248728583#B1JaelEMuVDSLJzKx2mWpFHOhFUQXN6dvVGMiVzVNhzUUJ4LWpXaIdzTHtUW4N5aFtUb5EjendGUWhFR0BVevAXaopndMRDZz36cURzMkp4TBN4azxkWjF4SyN5bHRXbTRXYmJVWmtWbHVFc9FTYM56LEFTdPtia836TykVZFdnakFTYwsWMFNlbVVkV64GajF4Yrwke7YkQ9IFWHhHNrl5SrJ7TZdnW9VXc5dWV5dWdXlkTNhGO7oENmFVVpJUNyJXVj36b8VGdOFzYz3mespFWyUzbMJXUohHZTpFVkRlYiojITJCLigjNyQTNxE4NiojIIJCL6IzN8ETO4ETO0IicfJye#4Xfd5nITdlM4IiOiMkIsIyNx8idg86bkRWQtIXZudWazVGRtMlSkFWZyB7UiojIOJyebpjIkJHUiwiI8MzN4EDMgcDMzADNyAjMiojI4J7QiwiIu6o9wWp9c+L0CeK0eeb9t6p9iojIh94QiwiIzgTN8IzN8QjM4QjM7UDNiojIklkI1pjIEJCLi4TPnlFbMRkcPVUS5JTYIhVeQZzNxkmZkFVUyoFb0V4b8h4ZOlEczdzR4NmV5N4YVdkYCVHVqJzLiZESEd5Uz8kSPFVdpVTar5GNCdkbxAzQVFncvFEd5pXUOl7Z6M7a9gHbwIXZktGVIV7KntGWNlzcJJ6T6ljefx"

export default {
  name: 'Viewer',
  components: {
  },
  data:function(){
      return {
        spread: null,
        hostStyle: { height: "580px", width: "100%", border: "solid gray 1px" },
        name: "",
        id:"",
        templateId: "",
        pdfSource:'',
        rule:"",
        disableInsertImage: true
      }
  },
  methods: {
    async workbookInitialized(value) {
      let self = this;
      this.spread = value;
        if(this.templateId){
            let template = await HttpUtils.get("template/get?id=" + this.templateId);
            let json = JSON.parse(Gzip.ungzipString(template.Content))
            this.spread.suspendPaint();
            this.spread.fromJSON(json);
            let data;

            if(this.id){
              let record = await HttpUtils.get("record/get?id=" + this.id);
              this.name = record.Name;
              data = JSON.parse(Gzip.ungzipString(record.Content));
            }
            if(!data){
              data = {
                记录编号:(new Date()).getTime().toString()
              };
            }
              if(json.designerBindingPathSchema && json.designerBindingPathSchema.properties){
                let properties = json.designerBindingPathSchema.properties
                for(var pro in properties){
                  let filed = properties[pro];
                  if(!data[pro] && filed && filed.dataFieldType == "table"){
                    data[pro] = [{},{},{},{}];
                  }
                }
              }
            let sheet = this.spread.getActiveSheet();
            let dataSource = new GC.Spread.Sheets.Bindings.CellBindingSource(data);
            sheet.setDataSource(dataSource);
            sheet.options.isProtected = true;
            sheet.options.rowHeaderVisible = false;
            sheet.options.colHeaderVisible = false;
            
            this.spread.options.scrollbarMaxAlign = true;
            this.spread.options.scrollbarShowMax = true;
            this.spread.options.tabStripVisible = false;
            // disable the vertical scrollbar
            this.spread.options.showVerticalScrollbar = false;
            // disable the horizontal scrollbar
            this.spread.options.showHorizontalScrollbar = false;
            processCellImage(sheet)
            processLock(sheet, this.rule)
            processTable(sheet, false, data);
            this.lookReport();
            this.spread.resumePaint();
            this.spread.bind(GC.Spread.Sheets.Events.EnterCell, (s, e)=>{
              let sheet = e.sheet, row = e.row, col = e.col;
              let tag = sheet.getTag(row, col);
              if(tag && tag["signature"]){
                self.disableInsertImage = false;
              }
              else{
                self.disableInsertImage = true;
              }
            })
        }

    },
    async insertImage() {
      let file = this.$refs.imageFile.files[0]
      if(file){
        let imageUrl = URL.createObjectURL(file);
        let sheet = this.spread.getActiveSheet(), row = sheet.getActiveRowIndex(), col = sheet.getActiveColumnIndex();
        let imageName = "img-"+row+"-"+col;
        var pic = sheet.pictures.get(imageName);
        if(pic){
          sheet.pictures.remove(imageName)
        }
        let src = await addCellImage(sheet, row, col, imageUrl, imageName);
        sheet.setValue(row, col, src);
      }
    },
    async saveRecord(){
        if(!this.name){
            alert("请输入模板名称");
            return;
        }
        var formData = new FormData();
        if(this.id){
            formData.append("id", this.id);
        }
        
        formData.append("templateId", this.templateId);
        formData.append("name", this.name);

        let sheet = this.spread.getActiveSheet();
        let recordData = sheet.getDataSource().getSource();
        formData.append("recordJSON", Gzip.gzipString(JSON.stringify(recordData)));
        // formData.append("recordJSON", JSON.stringify(recordData));
        formData.append("fullRecordJSON", Gzip.gzipString(JSON.stringify(this.spread.toJSON({includeBindingSource: true}))));
        // formData.append("fullRecordJSON", JSON.stringify(this.spread.toJSON({includeBindingSource: true})));
        debugger;
        let result = await HttpUtils.post("record/update", formData);
        if(result){
          this.id = result;
            Notification({title: '成功',message: "保存成功",type:'success'});
        }
     
    },
    goRcordList(){
        this.$router.push({ 
            path:'/record/list',
            query:{
                templateId: this.templateId
            }
        })
    },
    async lookReport(){
      var spreadJSON = JSON.stringify(this.spread.toJSON({ includeBindingSource: true }));
      var uploadData = Gzip.gzipString(spreadJSON);
        var formData = new FormData();
        formData.append("data", uploadData);

        let blob = await HttpUtils.post("report/getpdf", formData, {responseType: "blob"});
        const url = window.URL.createObjectURL(blob);
        // window.open(url, "_blank")
        this.pdfSource = url;
    },
  },
  created:function(){
      this.id = this.$route.query.id;
      this.templateId = this.$route.query.templateId;
      this.rule = this.$route.query.rule;
  },
}
</script>
<style scoped>
.designer{
    text-align: left;
}
</style>