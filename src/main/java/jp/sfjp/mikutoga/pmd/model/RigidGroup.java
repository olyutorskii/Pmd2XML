/*
 * rigid group
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 剛体グループ情報。
 * 剛体間の衝突設定の対象となる。
 */
public class RigidGroup implements SerialNumbered, Iterable<RigidInfo> {

    /** 剛体グループ総数。 */
    public static final int MAX_RIGID_GROUP = 16;


    private final List<RigidInfo> rigidList = new ArrayList<>();

    private int rigidGroupSerialNo = -1;


    /**
     * コンストラクタ。
     */
    public RigidGroup(){
        super();
        return;
    }


    /**
     * 所属する剛体のリストを返す。
     * @return 剛体リスト
     */
    public List<RigidInfo> getRigidList(){
        return this.rigidList;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Iterator<RigidInfo> iterator(){
        return this.rigidList.iterator();
    }

    /**
     * {@inheritDoc}
     * @param num {@inheritDoc}
     */
    @Override
    public void setSerialNumber(int num){
        this.rigidGroupSerialNo = num;
        return;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int getSerialNumber(){
        return this.rigidGroupSerialNo;
    }

    /**
     * グループ番号を返す。
     * MMDでは1〜16までが使われる。
     * 通し番号に1を加えた値と等しい。
     * @return グループ番号
     */
    public int getGroupNumber(){
        return this.rigidGroupSerialNo + 1;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String toString(){
        StringBuilder result = new StringBuilder();

        result.append("RigidGroup(").append(getGroupNumber()).append(") [");

        boolean dumped;

        dumped = false;
        for(RigidInfo rigid : this.rigidList){
            if(dumped) result.append(", ");
            result.append(rigid.getRigidName());
            dumped = true;
        }
        result.append(']');

        return result.toString();
    }

}
