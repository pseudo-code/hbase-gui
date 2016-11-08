# Overview / 
  This project is a GUI client for hbase database that built on java Swing.
  该项目是基于 java Swing 和 hbase java api 实现的一个GUI客户端，包含最简单的查看修改等功能。

# Features and limit / 功能和限制
  * Support view hbase rows and cells by pagination.
  * 支持以分页方式查看hbase的rows和cells
  * Support filter rows by RegEx.
  * 支持通过正则表达式对rows进行过滤
  * Support view/create/delete tables, view/create/delete rows, view/add/update cells.
  * 支持 查看/创建/删除table， 查看/创建/删除row， 查看/添加/更新cell
  * Not support view hbase data versions.
  * 默认只能操作最新单元格的最新版本，没有实现单元格不同版本的操作
 
# Requirements / 准备

## Enviroment / 环境
  * JDK 1.8 or a higher version
  * JDK 1.8或者更高的版本

## To Compile / 编译
  * Maven
  * 工程使用maven进行代码管理和编译

# Run / 运行
  By default, run from cn.ddb.hbase.App.main().
  默认情况下，通过cn.ddb.hbase.App.main() 进行运行。
