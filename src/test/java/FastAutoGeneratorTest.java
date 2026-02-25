import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.Collections;

public class FastAutoGeneratorTest {
    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/community_express?serverTimezone=Asia/Shanghai", "admin", "123456")
                .globalConfig(builder -> {
                    builder.author("ikaros") // 设置作者
                            .enableSpringdoc() // 开启 swagger 模式,Spring Boot 3 使用 enableSpringdoc() 替换 enableSwagger()
                            .outputDir(System.getProperty("user.dir") + "/src/main/java"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.express") // 设置父包名
                            .moduleName("system") // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, System.getProperty("user.dir") + "/src/main/resources/mapper")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("express_info") // 设置需要生成的表名，多张表用逗号分隔
                            .entityBuilder()
                            .enableLombok() // 开启 Lombok
                            .logicDeleteColumnName("is_deleted") // 逻辑删除字段名
                            .enableTableFieldAnnotation() // 开启字段注解
                            .controllerBuilder()
                            .enableRestStyle(); // 开启生成@RestController控制器
                })
                .strategyConfig(builder -> {
                    builder.addInclude("sys_user")
                            .entityBuilder()
                            .enableLombok()
                            .logicDeleteColumnName("is_deleted")
                            .enableTableFieldAnnotation()
                            .controllerBuilder()
                            .enableRestStyle();
                })
                .strategyConfig(builder -> {
                    builder.addInclude("shelf_info")
                            .entityBuilder()
                            .enableLombok()
                            .logicDeleteColumnName("is_deleted")
                            .enableTableFieldAnnotation()
                            .controllerBuilder()
                            .enableRestStyle();
                })
                .templateEngine(new VelocityTemplateEngine()) // 使用Velocity引擎模板
                .execute();
    }
}