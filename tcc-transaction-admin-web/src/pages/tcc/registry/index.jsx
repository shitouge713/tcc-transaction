import React, {useEffect, useState} from 'react';
import {registryOnline, registryOffline, registryQueryStatus} from "../../../common/api";
import {Button, Col, Popconfirm, Row, Space, Table} from "antd";

const Page = () => {
  const [registryStatusList, setRegistryStatusList] = useState([]);
  const [loadingStatus, setLoadingStatus] = useState(false); //加载数据

  useEffect(() => {
    reloadRegistryStatus(0);
  }, []);

  const reloadRegistryStatus = (delay) => {
    setLoadingStatus(true)
    setTimeout(() => {
      registryQueryStatus().then(data => {
        setLoadingStatus(false)
        setRegistryStatusList(data)
      }).catch((res) => {
        setLoadingStatus(false)
      })
    }, delay);
  };

  const columns =[
    {
      title: '实例',
      dataIndex: 'instance',
      render:(text, record)=>{
        return `${record.ip}:${record.port}`
      }
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (text, record) => {
        switch (text) {
          case 1:
            return "已上线";
          case 2:
            return "已下线";
          case 3:
            return "未知";
          default:
            return text;
        }
      }
    },
    {
      title: '操作',
      key: 'operation',
      render: (text,record)=>(
        <Space>
          {
            record.status !== 1?
              <Popconfirm
                  title="是否执行"
                  onConfirm={() => {
                    registryOnline(record.ip, record.port).then(resp => {
                      reloadRegistryStatus(3000)
                    })
                  }}
                  okText="是"
                  cancelText="否">
                <Button size="small" type="primary" danger>上线</Button>
              </Popconfirm>
              : <></>
          }
          {
            record.status !== 2?
                <Popconfirm
                    title="警告：实例下线可能会降低整体服务的可用性，请谨慎操作！"
                    onConfirm={() => {
                      registryOffline(record.ip,record.port).then(resp => {
                        reloadRegistryStatus(3000)
                      })
                    }}
                    okText="确认下线"
                    cancelText="取消">
                  <Button size="small" type="primary" danger>下线</Button>
                </Popconfirm>
                : <></>
          }
        </Space>

      )
    },
  ]
  return(
    <div>
      <Row style={{padding: '12px'}}>
        <Col span={23}>
          &nbsp;
        </Col>
        <Col span={1}>
          <Button type="primary"
                  size="small"
                  onClick={() => reloadRegistryStatus(0)}
                  disabled={loadingStatus}>
            刷新
          </Button>
        </Col>
      </Row>
      <Table rowKey={record => `${record.ip}:${record.port}`}
             dataSource={registryStatusList}
             columns={columns}
             pagination={false}
             loading={loadingStatus}/>
    </div>
  )
}

export default Page;
