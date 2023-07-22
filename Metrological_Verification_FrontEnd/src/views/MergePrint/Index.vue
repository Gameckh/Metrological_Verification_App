<template>
  <div class="design">
    <!-- <el-row>
      <el-col :span="3"
        ><el-button @click="$router.go(-1)">返回</el-button>
      </el-col>
      <el-col :span="3">
        <el-input
          v-model="name"
          :disabled="id !== undefined"
          placeholder="请输入记录名称"
        ></el-input>
      </el-col>
      <el-col :span="9">
        <el-button type="primary" @click="saveTemplate()">保存</el-button>
      </el-col>
    </el-row> -->
    <el-row style="margin-bottom: 5px">
      <el-button
        @click="customPrint()"
        style="float: right; margin-left: 20px"
        type="primary"
        size="medium"
        >打印
      </el-button>
      <el-button
        @click="insertAutomaticPageBreak()"
        style="float: right"
        type="primary"
        size="medium"
        v-bind:disabled="canInsertCustomBreak"
        >插入自动分页符
      </el-button>
    </el-row>
    <div style="height: 795px; width: 100%">
      <Designer @designerInitialized="designerInitialized"></Designer>
      <Designer1
        @designerInitialized="designerInitialized1"
        v-show="false"
      ></Designer1>
    </div>
  </div>
</template>

<script>
// @ is an alias to /src
import Designer from "../../components/Designer/Designer";
import HttpUtils from "../../utils/httpUtils";
import Gzip from "../../utils/gzip";
import { Loading, Notification } from "element-ui";
import Designer1 from "../../components/Designer/Designer.vue";
let designer = null;
let temp = null;
export default {
  name: "Index",
  components: {
    Designer,
    Designer1,
  },
  data: function () {
    return {
      name: "",
      id: this.$route.query.id,
      path: this.$route.query.path,
      schemaName: this.$route.query.schema,
      type: this.$route.query.type,
      reportTemplate: [],
      canInsertCustomBreak:true
    };
  },
  mounted: function () {
    Loading.service().close();
  },
  methods: {
    designerInitialized1(value) {
      temp = value.getWorkbook();
    },
    async designerInitialized(value) {
      debugger;
      designer = value;
      var spread = designer.getWorkbook();
      spread.suspendPaint();
      debugger;
      if(this.id[0] == "444444444"){
        this.canInsertCustomBreak = false;
      }else{
        this.canInsertCustomBreak = true;
      }
      for (let i = 0; i < this.id.length; i++) {
        if (this.path) {
          let template = await HttpUtils.get(
            this.path + "/get?id=" + this.id[i] + "&type=" + this.type,
            { responseType: "json" }
          );
          if (template) {
            // this.id = template.ID;
            this.name = template.Name;
            if (template.Content) {
              let json = JSON.parse(Gzip.ungzipString(template.Content));
              this.reportTemplate.push(json);
              // spread.fromJSON(json);
              // var fda = new FdaFunction();
              // spread.addCustomFunction(fda);
              if (json.designerBindingPathSchema) {
                designer.setData(
                  "treeNodeFromJson",
                  JSON.stringify(json.designerBindingPathSchema)
                );
                designer.setData(
                  "oldTreeNodeFromJson",
                  JSON.stringify(json.designerBindingPathSchema)
                );
              } else {
                this.setDefaultSchema();
              }
            }
          } else {
            this.setDefaultSchema();
          }
        }
      }
      console.log(this.reportTemplate);
      for (let i = 0; i < this.reportTemplate.length; i++) {
        // var designer =  new GC.Spread.Sheets.Designer.Designer(document.getElementById("gc-designer-container"));
        // var temp = designer.getWorkbook()
        temp.fromJSON(this.reportTemplate[i]);
        // 重命名样式表
        temp.getNamedStyles().forEach(function (namedStyle) {
          namedStyle.name = "sc" + i + "_" + namedStyle.name;
          spread.addNamedStyle(namedStyle);
        });

        var sheetCount = temp.getSheetCount();
        for (let j = 0; j < sheetCount; j++) {
          let sheet = temp.getSheet(j);
          let sheetJSON = JSON.stringify(sheet.toJSON());

          sheetJSON = sheetJSON.replace(
            /\"style\":\"/g,
            '"style":"sc' + i + "_"
          );

          var newSheet = new GC.Spread.Sheets.Worksheet("sheet" + i + "_");
          newSheet.fromJSON(JSON.parse(sheetJSON));
          newSheet.name(newSheet.name() + i + "_" + j);
          spread.addSheet(spread.getSheetCount(), newSheet);
        }
      }
      spread.removeSheet(0);
      spread.resumePaint();
      spread.setActiveSheet(5);
      spread.setActiveSheet(5);
    },
    async setDefaultSchema() {
      import(
        "../../components/Designer/bingdingSchema/" + this.schemaName + ".json"
      ).then((defaultSchema) => {
        designer.setData("treeNodeFromJson", JSON.stringify(defaultSchema));
        designer.setData("oldTreeNodeFromJson", JSON.stringify(defaultSchema));
      });
    },
    async saveTemplate() {
      if (!this.name) {
        alert("请输入模板名称");
        return;
      }
      var formData = new FormData();
      if (this.id) {
        formData.append("id", this.id);
      }
      formData.append("name", this.name);
      var spread = designer.getWorkbook();
      let json = spread.toJSON();
      let designerBindingPathSchema =
        designer.getData("updatedTreeNode") ||
        designer.getData("oldTreeNodeFromJson");
      if (designerBindingPathSchema) {
        json.designerBindingPathSchema = JSON.parse(designerBindingPathSchema);
      }

      formData.append("templateJSON", Gzip.gzipString(JSON.stringify(json)));

      let result = await HttpUtils.post(this.path + "/update", formData);
      if (result) {
        this.id = result;
        Notification({ title: "成功", message: "保存成功", type: "success" });
      }
    },
    insertAutomaticPageBreak() {
      let spread = designer.getWorkbook();
      let printSheet = spread.getActiveSheet();
      let activeRowIndex = printSheet.getActiveRowIndex();
      printSheet.isPrintLineVisible(true);
      printSheet.setRowPageBreak(activeRowIndex, true);
    },
    getPrintMaxLength() {},
    customPrint() {
      let spread = designer.getWorkbook();
      let printSheet = spread.getActiveSheet();
      let ActiveSheetIndex = spread.getActiveSheetIndex();
      // let activeRowIndex = printSheet.getActiveRowIndex();
      if(this.id[0] == "444444444"){
      let pages = spread.pageInfo(ActiveSheetIndex).pages;
      let printRowCount = 0;
      for (let i = 0; i < pages.length; i++) {
        printRowCount += pages[i].rowCount;
      }
      let pageBreakPosition = [];
      for (let i = 0; i < printRowCount; i++) {
        if (printSheet.getRowPageBreak(i)) {
          pageBreakPosition.push(i+1);
        }
      }
      let printMaxLength = 0;
      let currentPrintRowLength = 0;
      // 当pageInfo的分页和自己插入的分页符重合时则停止缩放
      if (pageBreakPosition.length > 0) {
        let printInfo = new GC.Spread.Sheets.Print.PrintInfo();
        for (let zoom = 1; zoom > 0; zoom-=0.05) {
          printInfo.zoomFactor(zoom);
          printSheet.printInfo(printInfo);
          let pagesNew = spread.pageInfo(ActiveSheetIndex).pages;
          let newPageBreakPosition = [];
          for (let i = 1; i < pagesNew.length; i++) {
            newPageBreakPosition.push(pagesNew[i].row+1)
          }
          if(pageBreakPosition.toString() == newPageBreakPosition.toString()){
             break;
          }
        }
        // printSheet.printInfo(printInfo);
        spread.print(ActiveSheetIndex);
        // 报表中只插入了一个分页符
        // if (pageBreakPosition.length == 1) {
        //   currentPrintRowLength = 0;
        //   for (let i = 0; i <= pageBreakPosition[0]; i++) {
        //     currentPrintRowLength += printSheet.getRowHeight(i);
        //   }
        //   printMaxLength =
        //     printMaxLength < currentPrintRowLength
        //       ? currentPrintRowLength
        //       : printMaxLength;
        //   currentPrintRowLength = 0;
        //   for (let i = pageBreakPosition[0] + 1; i <= printRowCount; i++) {
        //     currentPrintRowLength += printSheet.getRowHeight(i);
        //   }
        //   printMaxLength =
        //     printMaxLength < currentPrintRowLength
        //       ? currentPrintRowLength
        //       : printMaxLength;
        //   console.log(printMaxLength);
        //   // 报表中插入了一个以上的分页符
        // } else if (pageBreakPosition.length > 1) {
        //   for (let i = 0; i < pageBreakPosition.length; i++) {
        //     if (i == 0) {
        //       currentPrintRowLength = 0;
        //       for (let j = 0; j <= pageBreakPosition[0]; j++) {
        //         currentPrintRowLength += printSheet.getRowHeight(j);
        //       }
        //       printMaxLength =
        //         printMaxLength < currentPrintRowLength
        //           ? currentPrintRowLength
        //           : printMaxLength;
        //     } else {
        //       currentPrintRowLength = 0;
        //       for (
        //         let j = pageBreakPosition[i - 1] + 1;
        //         j <= pageBreakPosition[i];
        //         j++
        //       ) {
        //         currentPrintRowLength += printSheet.getRowHeight(j);
        //       }
        //       printMaxLength =
        //         printMaxLength < currentPrintRowLength
        //           ? currentPrintRowLength
        //           : printMaxLength;
        //     }
        //   }
        //   currentPrintRowLength = 0;
        //   for (
        //     let j = pageBreakPosition[pageBreakPosition.length - 1] + 1;
        //     j <= printRowCount;
        //     j++
        //   ) {
        //     currentPrintRowLength += printSheet.getRowHeight(j);
        //   }
        //   printMaxLength =
        //     printMaxLength < currentPrintRowLength
        //       ? currentPrintRowLength
        //       : printMaxLength;
        //   console.log(printMaxLength);
        // }
      } else {
        var printInfo = new GC.Spread.Sheets.Print.PrintInfo();
        // printInfo.bestFitRows(true);
        printInfo.fitPagesTall(1);
        printSheet.printInfo(printInfo);
        spread.print(ActiveSheetIndex);
      }
      console.log(printRowCount);
      }else{
        spread.print();
      }
    },
  },
  created: function () {},
};
</script>
<style scoped>
.design {
  text-align: left;
}
</style>