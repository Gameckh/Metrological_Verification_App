<template>
  <div class="design">
    <el-row>
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
        <el-button @click="lookReport()">预览报表</el-button>
      </el-col>
    </el-row>
    <div style="height: 100%; width: 100%">
      <Designer @designerInitialized="designerInitialized"></Designer>
    </div>
  </div>
</template>

<script>
// @ is an alias to /src
import Designer from "../../components/Designer/Designer";
import HttpUtils from "../../utils/httpUtils";
import Gzip from "../../utils/gzip";
import { Loading, Notification } from "element-ui";

export default {
  name: "Index",
  components: {
    Designer,
  },
  data: function () {
    return {
      designer: null,
      name: "",
      id: this.$route.query.id,
      path: this.$route.query.path,
      schemaName: this.$route.query.schema,
      type: this.$route.query.type,
    };
  },
  mounted: function () {
    Loading.service().close();
  },
  methods: {
    async designerInitialized(value) {
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
      debugger;
      this.designer = value;
      var spread = this.designer.getWorkbook();

      if (this.path) {
        let template = await HttpUtils.get(
          this.path + "/get?id=" + this.id + "&type=" + this.type,
          { responseType: "json" }
        );
        if (template) {
          this.id = template.ID;
          this.name = template.Name;
          if (template.Content) {
            let json = JSON.parse(Gzip.ungzipString(template.Content));
            spread.fromJSON(json);
            spread.refresh()
            if (json.designerBindingPathSchema) {
              this.designer.setData(
                "treeNodeFromJson",
                JSON.stringify(json.designerBindingPathSchema)
              );
              this.designer.setData(
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
    },
    async setDefaultSchema() {
      import(
        "../../components/Designer/bingdingSchema/" + this.schemaName + ".json"
      ).then((defaultSchema) => {
        this.designer.setData(
          "treeNodeFromJson",
          JSON.stringify(defaultSchema)
        );
        this.designer.setData(
          "oldTreeNodeFromJson",
          JSON.stringify(defaultSchema)
        );
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
      var spread = this.designer.getWorkbook();
      let json = spread.toJSON();
      let designerBindingPathSchema =
        this.designer.getData("updatedTreeNode") ||
        this.designer.getData("oldTreeNodeFromJson");
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
    async lookReport() {
      var spread = this.designer.getWorkbook();
      var spreadJSON = JSON.stringify(
        spread.toJSON({ includeBindingSource: true })
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
  },
  created: function () {},
};
</script>
<style scoped>
.design {
  text-align: left;
  height: 93%;
}
</style>