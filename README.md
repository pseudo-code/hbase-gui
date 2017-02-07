# Overview

This project is a GUI client for hbase database that built on java Swing.

# Features and limit

* Support view hbase rows and cells by pagination.
* Support filter rows by RegEx.
* Support view/create/delete tables, view/create/delete rows, view/add/update cells.
* Not support view hbase data versions.

# Enviroment

JDK 1.8 or a higher version

maven

# Run
By default, run from cn.ddb.hbase.App.main().

---

#　概述

该项目是基于 java Swing 和 hbase java api 实现的一个GUI客户端，包含最简单的查看修改等功能。

# 功能和限制

* 支持以分页方式查看hbase的rows和cells
* 支持通过正则表达式对rows进行过滤
* 支持 查看/创建/删除tables， 查看/创建/删除rows， 查看/添加/更新cells
* 默认只能操作最新单元格的最新版本，没有实现单元格不同版本的操作

# 环境

JDK 1.8或者更高的版本

工程使用maven进行代码管理和编译

# 运行

默认情况下，通过cn.ddb.hbase.App.main() 进行运行。
