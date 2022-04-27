### 请求参数
| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | --- | --- | ---- |
| term | 1 | Integer | 是 | leader当前任期 |
| leaderId | eyJhbGciOiJIUzUxMiJ9 | String | 是 | leader节点的clientId |
| entries | 见下面例子 | Array | 是 | 携带的日志 |
| preLogTerm | 1 | Integer | 是 | 前一条日志的term |
| preLogIndex | 1 | Integer | 是 | 前一条日志的index |
| leaderCommit | 1 | Integer | 是 | leader的commitIndex |

entries 例：[{"index":1,"term":1,"command":{"key":"a","operate":"set","value":"1"}}]

### 响应值
| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | --- | --- | ---- |
| clientId | aTcR1_JjmF9JcT4rMWR | String | 是 | 客户端id |
| term | 1 | Integer | 是 | 客户端当前任期 |
| success | true | Boolean | 是 | 日志复制是否成功 |
| conflictTerm | 1 | Integer | 否 | 日志冲突的term |
| nextIndexSuggest | 1 | Integer | 否 | 下一次传输日志建议的index |

### 请求参数
| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | --- | --- | ---- |
| term | 1 | Integer | 是 | 候选人当前任期 |
| candidateId | eyJhbGciOiJIUzUxMiJ9 | String | 是 | candidate节点的clientId |
| lastLogIndex | 10 | Integer | 是 | 最后一条日志的索引 |
| lastLogTerm | 1 | Integer | 是 | 最后一条日志的term |

### 响应值
| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | --- | --- | ---- |
| clientId | aTcR1_JjmF9JcT4rMWR | String | 是 | 响应节点客户端id |
| term | 1 | Integer | 是 | 客户端当前任期 |
| voteGranted | true | Boolean | 是 | 是否赞成投票 |


