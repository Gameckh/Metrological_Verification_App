<template>
  <div class="designer">
     <el-row class="toolbar">
      <el-col :span="6">
        <el-button-group>
      <el-button @click="goRcordList()">填报记录</el-button>
      </el-button-group>
      </el-col>
      </el-row>
      <div>
          <gc-spread-sheets
            :hostStyle="hostStyle"
            @workbookInitialized="workbookInitialized"
          ></gc-spread-sheets>
      </div>
  </div>
</template>

<script>
// @ is an alias to /src

import "@grapecity-software/spread-sheets/styles/gc.spread.sheets.excel2013white.css";
import * as GC from "@grapecity-software/spread-sheets";
import "@grapecity-software/spread-sheets-resources-zh";
import "@grapecity-software/spread-sheets-vue";

GC.Spread.Common.CultureManager.culture("zh-cn");
import HttpUtils from '../../utils/httpUtils'
GC.Spread.Sheets.LicenseKey = "guanyuanshuju,695962798612895#B1JRKVohEUnRTZ9c7bw2kNFJjb8FnMH56NLFWYi9ERMdlbtFnYlVDUjFlYYJDTo36NIxUORRGUHFmWWdzQwx4SHlHbJhmZzM6YhxWRzAVSyh4TwYDNQF5dr56ZwQnd9JzTHRGZGdFWjJXd6hkaOpEVwknMHxUS9pUVmlUOxwmVzNzcCJzZQN5avRWMzVlesNGTEVkZ0NnWLJ5UjJmc7IHTQlURzsGaycWSuN5NjF7UvZnMwAnWYZkZ85EOTtyQ7EWOMJjTQp5cxITc4REZmlVc4olYGlmMMJTNlJEUHFTOnJlQHRVU7YWViojITJCLiETR7IDM8MkNiojIIJCLzgDM9QjNyYzN0IicfJye#4Xfd5nIVF4SRJiOiMkIsIyNx8idgMlSgQWYlJHcTJiOi8kI1tlOiQmcQJCLiMDNzQTMwAyNwMDM4IDMyIiOiQncDJCLi8ajmDblmz9voL8po19tl7anmLiOiEmTDJCLiUTO8ITM6gTO7IjN9UTO6IiOiQWSiwSfdtlOicGbmJCLlNHbhZmOiI7ckJye0ICbuFkI1pjIEJCLi4TPnRkV8QDMOlXbM34TWFTd9FTM8Mmb95mQUR6Vmx4dzsyQSdzZBdTNp9kRUBXRGB5MLN6MVhGMJNlN4sERQVXWotmYDpXTjNVYxsWRqJEWRVVeLlkWpZWNPNGN9olUlF4UyZVazckWlVnWwhkN4RIK";
// GC.Spread.Sheets.Designer.LicenseKey = "guanyuanshuju,457244248728583#B1JaelEMuVDSLJzKx2mWpFHOhFUQXN6dvVGMiVzVNhzUUJ4LWpXaIdzTHtUW4N5aFtUb5EjendGUWhFR0BVevAXaopndMRDZz36cURzMkp4TBN4azxkWjF4SyN5bHRXbTRXYmJVWmtWbHVFc9FTYM56LEFTdPtia836TykVZFdnakFTYwsWMFNlbVVkV64GajF4Yrwke7YkQ9IFWHhHNrl5SrJ7TZdnW9VXc5dWV5dWdXlkTNhGO7oENmFVVpJUNyJXVj36b8VGdOFzYz3mespFWyUzbMJXUohHZTpFVkRlYiojITJCLigjNyQTNxE4NiojIIJCL6IzN8ETO4ETO0IicfJye#4Xfd5nITdlM4IiOiMkIsIyNx8idg86bkRWQtIXZudWazVGRtMlSkFWZyB7UiojIOJyebpjIkJHUiwiI8MzN4EDMgcDMzADNyAjMiojI4J7QiwiIu6o9wWp9c+L0CeK0eeb9t6p9iojIh94QiwiIzgTN8IzN8QjM4QjM7UDNiojIklkI1pjIEJCLi4TPnlFbMRkcPVUS5JTYIhVeQZzNxkmZkFVUyoFb0V4b8h4ZOlEczdzR4NmV5N4YVdkYCVHVqJzLiZESEd5Uz8kSPFVdpVTar5GNCdkbxAzQVFncvFEd5pXUOl7Z6M7a9gHbwIXZktGVIV7KntGWNlzcJJ6T6ljefx"

export default {
  name: 'AllData',
  components: {
  },
  data:function(){
      return {
        spread: null,
        hostStyle: { height: "580px", width: "100%", border: "solid gray 1px" },
        templateId: this.$route.query.templateId,
      }
  },
  methods: {
    async workbookInitialized(value) {
      this.spread = value;
        if(this.templateId){
          this.spread.suspendPaint();
            let record = await HttpUtils.get("record/getAll?templateId=" + this.templateId);
            let sheet = this.spread.getActiveSheet();
            sheet.setDataSource(record);
            this.spread.resumePaint();
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