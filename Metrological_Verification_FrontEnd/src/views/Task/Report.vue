<template>
  <div class="designer">
     <el-row class="toolbar">
      <el-col :span="6">
        <el-button-group>
      <el-button @click="goRcordList()">返回列表</el-button>
      </el-button-group>
        <el-button-group>
      <el-button type="success" @click="viewFullReport()">完整报告</el-button>
      </el-button-group>
      </el-col>
      </el-row>
     <el-row>
      <el-col :span="12">
        <el-tabs v-model="activeTab" @tab-click="handleClick">
          <el-tab-pane label="检测报告" name="reportSpread">
                <gc-spread-sheets
                  :hostStyle="hostStyle"
                  @workbookInitialized="reportworkbookInitialized"
                ></gc-spread-sheets>
          </el-tab-pane>
          <el-tab-pane label="检测记录" name="recordSpread">
                <gc-spread-sheets
                  :hostStyle="hostStyle"
                  @workbookInitialized="recordworkbookInitialized"
                ></gc-spread-sheets></el-tab-pane>
          <el-tab-pane label="委托单" name="taskSpread">
                <gc-spread-sheets
                  :hostStyle="hostStyle"
                  @workbookInitialized="taskworkbookInitialized"
                ></gc-spread-sheets></el-tab-pane>
        </el-tabs>
      </el-col>

      <el-col :span="12">
          <embed type="" :src="pdfSource" v-if="pdfSource" style="margin-left:10px" width="100%" height="580px">
      </el-col>
      </el-row>

      <div>
        <div style="width:98%;  ">
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
import "@grapecity-software/spread-sheets-barcode"

GC.Spread.Common.CultureManager.culture("zh-cn");
import HttpUtils from '../../utils/httpUtils'
import Gzip from '../../utils/gzip'
import { Notification } from 'element-ui';
import { processCellImage, setViewMode, processTable, processLock } from '../../components/SpreadSheets/common'
GC.Spread.Sheets.LicenseKey = "guanyuanshuju,695962798612895#B1JRKVohEUnRTZ9c7bw2kNFJjb8FnMH56NLFWYi9ERMdlbtFnYlVDUjFlYYJDTo36NIxUORRGUHFmWWdzQwx4SHlHbJhmZzM6YhxWRzAVSyh4TwYDNQF5dr56ZwQnd9JzTHRGZGdFWjJXd6hkaOpEVwknMHxUS9pUVmlUOxwmVzNzcCJzZQN5avRWMzVlesNGTEVkZ0NnWLJ5UjJmc7IHTQlURzsGaycWSuN5NjF7UvZnMwAnWYZkZ85EOTtyQ7EWOMJjTQp5cxITc4REZmlVc4olYGlmMMJTNlJEUHFTOnJlQHRVU7YWViojITJCLiETR7IDM8MkNiojIIJCLzgDM9QjNyYzN0IicfJye#4Xfd5nIVF4SRJiOiMkIsIyNx8idgMlSgQWYlJHcTJiOi8kI1tlOiQmcQJCLiMDNzQTMwAyNwMDM4IDMyIiOiQncDJCLi8ajmDblmz9voL8po19tl7anmLiOiEmTDJCLiUTO8ITM6gTO7IjN9UTO6IiOiQWSiwSfdtlOicGbmJCLlNHbhZmOiI7ckJye0ICbuFkI1pjIEJCLi4TPnRkV8QDMOlXbM34TWFTd9FTM8Mmb95mQUR6Vmx4dzsyQSdzZBdTNp9kRUBXRGB5MLN6MVhGMJNlN4sERQVXWotmYDpXTjNVYxsWRqJEWRVVeLlkWpZWNPNGN9olUlF4UyZVazckWlVnWwhkN4RIK";
// GC.Spread.Sheets.Designer.LicenseKey = "guanyuanshuju,457244248728583#B1JaelEMuVDSLJzKx2mWpFHOhFUQXN6dvVGMiVzVNhzUUJ4LWpXaIdzTHtUW4N5aFtUb5EjendGUWhFR0BVevAXaopndMRDZz36cURzMkp4TBN4azxkWjF4SyN5bHRXbTRXYmJVWmtWbHVFc9FTYM56LEFTdPtia836TykVZFdnakFTYwsWMFNlbVVkV64GajF4Yrwke7YkQ9IFWHhHNrl5SrJ7TZdnW9VXc5dWV5dWdXlkTNhGO7oENmFVVpJUNyJXVj36b8VGdOFzYz3mespFWyUzbMJXUohHZTpFVkRlYiojITJCLigjNyQTNxE4NiojIIJCL6IzN8ETO4ETO0IicfJye#4Xfd5nITdlM4IiOiMkIsIyNx8idg86bkRWQtIXZudWazVGRtMlSkFWZyB7UiojIOJyebpjIkJHUiwiI8MzN4EDMgcDMzADNyAjMiojI4J7QiwiIu6o9wWp9c+L0CeK0eeb9t6p9iojIh94QiwiIzgTN8IzN8QjM4QjM7UDNiojIklkI1pjIEJCLi4TPnlFbMRkcPVUS5JTYIhVeQZzNxkmZkFVUyoFb0V4b8h4ZOlEczdzR4NmV5N4YVdkYCVHVqJzLiZESEd5Uz8kSPFVdpVTar5GNCdkbxAzQVFncvFEd5pXUOl7Z6M7a9gHbwIXZktGVIV7KntGWNlzcJJ6T6ljefx"

export default {
  name: 'Report',
  components: {
  },
  data:function(){
      return {
        reportSpread: null,
        recordSpread: null,
        taskSpread: null,
        hostStyle: { height: "550px", width: "100%" },
        id:this.$route.query.id,
        type: 'report',
        activeTab: "reportSpread",
        pdfSource: null
      }
  },
  methods: {
    async reportworkbookInitialized(value) {
      this.reportSpread = value;
      if(!this.id){
        return;
      }
      setViewMode(this.reportSpread)

      let task = await HttpUtils.get("task/getTask?id=" + this.id + "&type=" + this.type);
      
      task.TaskTemplateContent = JSON.parse(Gzip.ungzipString(task.TaskTemplateContent))
      task.RecordTemplateContent = JSON.parse(Gzip.ungzipString(task.RecordTemplateContent))
      task.ReportTemplateContent = JSON.parse(Gzip.ungzipString(task.ReportTemplateContent))

      task.TaskContent = JSON.parse(Gzip.ungzipString(task.TaskContent));
      let recordJSON = JSON.parse(Gzip.ungzipString(task.RecordContent));
      task.ReportContent = JSON.parse(Gzip.ungzipString(task.RecordContent));
      task.ReportContent["报告单名称"] = task.TestName + "检测报告";
      task.RecordContent = recordJSON;
          
      this.initWorkbook(this.reportSpread, task.ReportTemplateContent, task.ReportContent);
      this.initWorkbook(this.recordSpread, task.RecordTemplateContent, task.RecordContent);
      this.initWorkbook(this.taskSpread, task.TaskTemplateContent, task.TaskContent);
      this.lookReport(this.reportSpread)
    },

    async recordworkbookInitialized(value) {
      this.recordSpread = value;
    },
    async taskworkbookInitialized(value) {
      this.taskSpread = value;
    },
    initWorkbook(spread, json, data){
      spread.suspendPaint();
      spread.fromJSON(json);
      setViewMode(spread)
      let sheet = spread.getActiveSheet();
      sheet.options.isProtected = true;
      processTable(sheet, true, data);
      let dataSource = new GC.Spread.Sheets.Bindings.CellBindingSource(data);
      sheet.setDataSource(dataSource);
      processCellImage(sheet)
      spread.resumePaint();
    },
    goRcordList(){
        this.$router.push({ 
            path:'/task/list',
        })
    },
      handleClick(tab, event) {
        let spread = this[this.activeTab];
        spread.getHost().style.display = "none"
        setTimeout(function(){
          spread.refresh();
        spread.getHost().style.display = "block"
        },10)
        this.lookReport(spread);
      },
    async lookReport(spread){
      var spreadJSON = JSON.stringify(spread.toJSON({ includeBindingSource: true }));
      var uploadData = Gzip.gzipString(spreadJSON);
        var formData = new FormData();
        formData.append("data", uploadData);
        let blob = await HttpUtils.post("report/getpdf", formData, {responseType: "blob"});
        const url = window.URL.createObjectURL(blob);
        // window.open(url, "_blank")
        this.pdfSource = url;
    },
    async viewFullReport(){
      var reportSpreadJSON = JSON.stringify(this.reportSpread.toJSON({ includeBindingSource: true }));
      var recordSpreadJSON = JSON.stringify(this.recordSpread.toJSON({ includeBindingSource: true }));
      var taskSpreadJSON = JSON.stringify(this.taskSpread.toJSON({ includeBindingSource: true }));
        var formData = new FormData();
        formData.append("content1", Gzip.gzipString(reportSpreadJSON));
        formData.append("content2", Gzip.gzipString(recordSpreadJSON));
        formData.append("content3", Gzip.gzipString(taskSpreadJSON));

        let blob = await HttpUtils.post("report/getfullpdf", formData, {responseType: "blob"});
        const url = window.URL.createObjectURL(blob);
        window.open(url, "_blank")
    }
   
  },
  created:function(){
  },
}
</script>
<style scoped>
.designer{
    text-align: left;
}
</style>