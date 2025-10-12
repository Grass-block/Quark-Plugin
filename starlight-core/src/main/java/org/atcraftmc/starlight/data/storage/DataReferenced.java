package org.atcraftmc.starlight.data.storage;

public interface DataReferenced extends DataReference{
    DataReference getReference();

    void setReference(DataReference reference);

    @Override
    default void save(){
        this.getReference().save();
    }
}
