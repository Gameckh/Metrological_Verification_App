<template>
  <div class="designer">
    <el-row class="toolbar">
      <el-col :span="24">
        <el-button-group>
          <el-button type="primary" @click="saveTask()">保存</el-button>
        </el-button-group>

        <el-button-group>
          <input type="file" ref="imageFile" :disabled="disableInsertImage" />
          <el-button @click="insertImage()" :disabled="disableInsertImage"
            >插入图片</el-button
          >
        </el-button-group>

        <el-button-group>
          <el-button @click="lookReport()">预览报表</el-button>
          <el-button @click="goRcordList()">返回列表</el-button>
        </el-button-group>
      </el-col>
    </el-row>
    <div>
      <div style="width: 98%">
        <gc-spread-sheets
          :hostStyle="hostStyle"
          @workbookInitialized="workbookInitialized"
        ></gc-spread-sheets>
      </div>
    </div>
  </div>
</template>

<script>
// @ is an alias to /src

import "@grapecity/spread-sheets/styles/gc.spread.sheets.excel2013white.css";
import * as GC from "@grapecity/spread-sheets";
import "@grapecity/spread-sheets-resources-zh";
import "@grapecity/spread-sheets-vue";
import "@grapecity/spread-sheets-print";

GC.Spread.Common.CultureManager.culture("zh-cn");
import HttpUtils from "../../utils/httpUtils";
import Gzip from "../../utils/gzip";
import { Notification } from "element-ui";
import {
  processCellImage,
  processLock,
  setViewMode,
  processTable,
  addCellImage,
} from "../../components/SpreadSheets/common";

export default {
  name: "New",
  components: {},
  data: function () {
    return {
      spread: null,
      hostStyle: { height: "580px", width: "1000px" },
      id: this.$route.query.id,
      rule: this.$route.query.rule,
      type: this.$route.query.type || "task",
      disableInsertImage: true,
    };
  },
  methods: {
    async workbookInitialized(value) {
      var FdaFunction = function () {
        this.name = "FDA";
        this.minArgs = 1;
        this.maxArgs = 2;
      };
      FdaFunction.prototype = new GC.Spread.CalcEngine.Functions.Function();
      FdaFunction.prototype.description = function () {
        return {
          description: "对value进行四舍六入五留双修约，保留小数点后指定位数",
          parameters: [
            {
              name: "value",
              repeatable: false,
              optional: false,
            },
            {
              name: "places",
              repeatable: false,
              optional: false,
            },
          ],
        };
      };
      FdaFunction.prototype.isContextSensitive = function () {
        return true;
      };
      FdaFunction.prototype.evaluate = function (context, num, places) {
        if (!isNaN(parseInt(num)) && !isNaN(parseInt(places))) {
          console.log("evaluate");
          num = numGeneral(num);
          if (!isNumber(num)) {
            return num;
          }
          var d = places || 0;
          var m = Math.pow(10, d);
          var n = +(d ? num * m : num).toFixed(8); // Avoid rounding errors
          var i = Math.floor(n),
            f = n - i;
          var e = 1e-8; // Allow for rounding errors in f
          var r =
            f > 0.5 - e && f < 0.5 + e
              ? i % 2 == 0
                ? i
                : i + 1
              : Math.round(n);
          var result = d ? r / m : r;

          if (places > 0) {
            var s_x = result.toString();
            var pos_decimal = s_x.indexOf(".");
            if (pos_decimal < 0) {
              pos_decimal = s_x.length;
              s_x += ".";
            }
            while (s_x.length <= pos_decimal + places) {
              s_x += "0";
            }
            return s_x;
          } else {
            return result;
          }
        } else {
          return "#VALUE!";
        }
      };

      function numGeneral(val, mand, type) {
        console.log("numGeneral");
        var matCheck = function (v) {
          return String(v).match(
            /^([\D\-(?!\d)\.(?!\d)]*?)((\-?\d*\.?\d+)((e|E)(\+|\-)?(\d+))?)(.*)$/
          );
        };
        var reVal = matCheck(val);
        if (reVal) {
          if (typeof mand == "string" && !type) type = mand;
          var reMatch = function (matchs) {
            var num = matchs[2],
              res = "",
              last = matchs[8] || "",
              isF = Number.isFinite || isFinite;
            if (type === "number") {
              return [Number(num), last];
            } else if (matchs[4]) {
              var p1 = "",
                p2 = "",
                pm = 1;
              if (num.indexOf("-") === 0) (pm = 0), (num = num.substr(1));
              num = num.replace(/^0+/, "").replace(/^\./, "0.");
              var dot = num.indexOf("."),
                power = Number(matchs[7]) || 0,
                ONnum = num
                  .replace(/(e|E).+$/, "")
                  .replace(/(\.\d+?)0+$/, "$1")
                  .replace(/\.0+$|\./g, "");
              if (dot != -1) {
                (p1 = ONnum.substring(0, dot)),
                  (p2 = ONnum.substring(dot, ONnum.length));
              }
              if (matchs[6] == "-") {
                if (dot == -1) {
                  ONnum = ONnum.replace(/^0+/, "");
                  if (power < ONnum.length) {
                    res =
                      ONnum.substr(0, ONnum.length - power) +
                      "." +
                      ONnum.substr(-power);
                  } else {
                    for (var i = 0, le = power - ONnum.length; i < le; i++)
                      ONnum = "0" + ONnum;
                    res = "0." + ONnum;
                  }
                } else {
                  if (!/[^0]/.test(p1)) {
                    p1 = "0.";
                    for (var i = 0; i < power; i++) p1 += "0";
                  } else if (power < p1.length) {
                    p1 =
                      p1.substr(0, p1.length - power) + "." + p1.substr(-power);
                  } else {
                    for (var i = 0, le = power - p1.length; i < le; i++)
                      p1 = "0" + p1;
                    p1 = "0." + p1;
                  }
                  res = p1 + p2;
                }
              } else {
                if (dot == -1) {
                  res = ONnum.replace(/^0+/, "");
                  for (var i = 0; i < power; i++) res += "0";
                } else {
                  if (power < p2.length) {
                    p2 = p2.substr(0, power) + "." + p2.substr(power);
                  } else {
                    for (var i = 0, le = power - p2.length; i < le; i++)
                      p2 += "0";
                  }
                  res = p1 + p2;
                }
              }
              res = res
                .replace(/^0+(?!\.)/, "")
                .replace(/(\.\d+?)0+$/, "$1")
                .replace(/\.0+$|\.$/, "");
              if (isF(Number(res)) && !String(Number(res)).match(/e|E/)) {
                res = pm < 1 ? -Number(res) : Number(res);
              } else {
                res = pm < 1 ? "-" + res : res;
              }
            } else {
              res =
                matchs[3] &&
                isF(Number(matchs[3])) &&
                !matCheck(Number(matchs[3]))[4]
                  ? Number(matchs[3])
                  : num;
            }
            return [type === "string" ? String(res) : res, last];
          };
          if (reVal[8] && mand == true) {
            var has = true,
              matARR = [reMatch(reVal)];
            var numARR = [matARR[0][0]];
            while (has == true) {
              var lastMat = matCheck(matARR[matARR.length - 1][1]);
              if (lastMat) {
                var last = reMatch(lastMat);
                matARR.push(last), numARR.push(last[0]);
              } else has = false;
            }
            return numARR.length > 1 ? numARR : matARR[0][0];
          } else {
            return reMatch(reVal)[0];
          }
        } else {
          return val;
        }
      }

      function isNumber(val) {
        console.log("isNumber");
        var regPos = /^\d+(\.\d+)?$/; //非负浮点数
        var regNeg =
          /^(-(([0-9]+\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\.[0-9]+)|([0-9]*[1-9][0-9]*)))$/; //负浮点数
        if (regPos.test(val) || regNeg.test(val)) {
          return true;
        } else {
          return false;
        }
      }
      var fda = new FdaFunction();
      GC.Spread.CalcEngine.Functions.defineGlobalCustomFunction("FDA",fda);
      let self = this;
      this.spread = value;

      if (!this.id) {
        return;
      }
      setViewMode(this.spread);
      this.spread.suspendPaint();

      let task = await HttpUtils.get(
        "task/getTask?id=" + this.id + "&type=" + this.type
      );
      if (this.type === "task" && task.TaskTemplateContent) {
        let json = JSON.parse(Gzip.ungzipString(task.TaskTemplateContent));
        var fda = new FdaFunction();
        this.spread.addCustomFunction(fda);
        this.spread.fromJSON(json);
      } else if (this.type === "record" && task.RecordTemplateContent) {
        let json = JSON.parse(Gzip.ungzipString(task.RecordTemplateContent));
        this.spread.fromJSON(json);
      }
      setViewMode(this.spread);

      let data;
      if (this.type === "task") {
        if (task.TaskContent) {
          data = JSON.parse(Gzip.ungzipString(task.TaskContent));
        } else {
          data = {
            检测单位: task.TestCompany,
            委托编号: task.TaskNumber,
            委托单名称: task.TestName + "委托单",
          };
        }
      } else if (this.type === "record") {
        if (task.RecordContent) {
          data = JSON.parse(Gzip.ungzipString(task.RecordContent));
        } else if (task.TaskContent) {
          data = JSON.parse(Gzip.ungzipString(task.TaskContent));
          data["检测单名称"] = task.TestName + "检测记录表";
          data["记录编号"] = "REC" + new Date().getTime();
          data["实验记录"] = [];
          for (var i = 0; i < data["检测次数"]; i++) {
            data["实验记录"].push({});
          }
        }
      }

      let sheet = this.spread.getActiveSheet();
      processTable(sheet, false, data);
      let dataSource = new GC.Spread.Sheets.Bindings.CellBindingSource(data);
      sheet.setDataSource(dataSource);
      processCellImage(sheet);
      processLock(sheet, this.rule);
      this.spread.resumePaint();

      this.spread.bind(GC.Spread.Sheets.Events.EnterCell, (s, e) => {
        let sheet = e.sheet,
          row = e.row,
          col = e.col;
        let tag = sheet.getTag(row, col);
        if (tag && tag["signature"]) {
          self.disableInsertImage = false;
        } else {
          self.disableInsertImage = true;
        }
      });
    },

    async saveTask() {
      let sheet = this.spread.getActiveSheet();
      let recordData = sheet.getDataSource().getSource();

      var formData = new FormData();
      formData.append("id", this.id);
      formData.append("type", this.type);
      formData.append("content", Gzip.gzipString(JSON.stringify(recordData)));

      let result = await HttpUtils.post("task/saveTask", formData);
      if (result) {
        Notification({ title: "成功", message: "保存成功", type: "success" });
      }
    },
    goRcordList() {
      this.$router.push({
        path: "/task/list",
      });
    },
    async lookReport() {
      debugger;
      var spreadJSON = JSON.stringify(
        this.spread.toJSON({ includeBindingSource: true })
      );
      var uploadData = Gzip.gzipString(spreadJSON);
      var formData = new FormData();
      formData.append("data", uploadData);

      let blob = await HttpUtils.post("report/getpdf", formData, {
        responseType: "blob",
      });
      const url = window.URL.createObjectURL(blob);
      window.open(url, "_blank");
    },
    async insertImage() {
      let file = this.$refs.imageFile.files[0];
      if (file) {
        let imageUrl = URL.createObjectURL(file);
        let sheet = this.spread.getActiveSheet(),
          row = sheet.getActiveRowIndex(),
          col = sheet.getActiveColumnIndex();
        let imageName = "img-" + row + "-" + col;
        var pic = sheet.pictures.get(imageName);
        if (pic) {
          sheet.pictures.remove(imageName);
        }
        let src = await addCellImage(sheet, row, col, imageUrl, imageName);
        sheet.setValue(row, col, src);
      }
    },
  },
  created: function () {},
};
</script>
<style scoped>
.designer {
  text-align: left;
}
</style>