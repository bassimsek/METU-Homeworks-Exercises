#include <fcntl.h>
#include <iostream>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <ctime>
#include <string.h>
#include <string>
#include <unistd.h>
#include <vector>

#include "ext2fs.h"


typedef unsigned char bitmapDef;


#define RESERVED_INODE_COUNT 12
#define OFFSET_BLOCK(block) (blockSize * block)


bool isNumber(std::string arg);
unsigned int iNodeBlockGroupNumber(unsigned int inodeNumber);
unsigned int iNodeIndex(unsigned int inodeNumber);
unsigned int iNodeTableAddress(unsigned iNodeBlockGroupNumber);
void readInode(unsigned int inodeNumber, struct ext2_inode *inode);
void writeInode(unsigned int inodeNumber, struct ext2_inode *inode);
unsigned int findInodeNumber(unsigned int inodeNo, char* dirName);
unsigned int realEntrySize(uint8_t name_len);
unsigned int roundUp4(uint32_t real_len);

void dupTask( unsigned int source_inode_number, unsigned int dest_inode_number, char *destFileName);



unsigned int numberOfGroups = 0;

struct ext2_super_block *superBlock;
unsigned int blockSize = 0;
uint32_t inodesPerGroup;
uint16_t inodeSize;

struct ext2_block_group_descriptor *groupDescriptors; 

unsigned int img;




void setBit(unsigned int bit, bitmapDef *bitmap) {

    unsigned int numberOfBits = ((int) sizeof (bitmapDef)) * 8;
    unsigned int positionInBytes = bit / numberOfBits; // first 8 bit = first byte and so on
    bitmapDef setter = (bitmapDef) 1 << (bit % numberOfBits); // create 8 bits bitmapDef contains one 1 according to given bit

    bitmap[positionInBytes] = bitmap[positionInBytes] | setter; // bitwise OR for setting given bit
}




bool isBitSet(unsigned int bit, bitmapDef *bitmap) {

    unsigned int numberOfBits = ((int) sizeof (bitmapDef)) * 8;
    unsigned int positionInBytes = bit / numberOfBits; // first 8 bit = first byte and so on
    bitmapDef setter = (bitmapDef) 1 << (bit % numberOfBits); // create 8 bits bitmapDef contains one 1 according to given bit

    unsigned char res = bitmap[positionInBytes] & setter; // bitwise AND for given bit to check against zero.

    return res != 0; // check result against zero.
}




bool isNumber(std::string arg) {

    for (int i = 0; i < arg.length(); i++) {
        if (!isdigit(arg[i])) {
            return false;
        }
    }     
    return true;
}



unsigned int iNodeBlockGroupNumber(unsigned int inodeNumber) {
    return (inodeNumber - 1) / inodesPerGroup;
}


unsigned int iNodeIndex(unsigned int inodeNumber) {
    return (inodeNumber - 1) % inodesPerGroup;
}


unsigned int iNodeTableAddress(unsigned iNodeBlockGroupNumber) {
    return groupDescriptors[iNodeBlockGroupNumber].inode_table;
}


void readInode(unsigned int inodeNumber, struct ext2_inode *inode) {
    unsigned int blockGroupNumber = iNodeBlockGroupNumber(inodeNumber);
    lseek(img, OFFSET_BLOCK(iNodeTableAddress(blockGroupNumber)) + iNodeIndex(inodeNumber) * inodeSize, SEEK_SET); 
    read(img, inode, sizeof(struct ext2_inode));
}


void writeInode(unsigned int inodeNumber, struct ext2_inode *inode) {
    unsigned int blockGroupNumber = iNodeBlockGroupNumber(inodeNumber);
    lseek(img, OFFSET_BLOCK(iNodeTableAddress(blockGroupNumber)) + iNodeIndex(inodeNumber) * inodeSize, SEEK_SET); 
    write(img, inode, sizeof(struct ext2_inode));
}




unsigned int findInodeNumber(unsigned int inodeNo, char* dirName) {
   
    struct ext2_inode currInode;
    readInode(inodeNo, &currInode);

    // read one block
    unsigned int remainingSize = blockSize;

    unsigned int dirNameLen = strlen(dirName);
    
    // dir_entry temp fields
    uint32_t inode;
    uint16_t length;
    uint8_t name_length;
    uint8_t file_type;
    char name[EXT2_MAX_NAME_LENGTH];

    char flag = 0;

    for (int i = 0; i < EXT2_NUM_DIRECT_BLOCKS; i++) {
        if (currInode.direct_blocks[i] != 0) {
            remainingSize = blockSize;
            lseek(img, OFFSET_BLOCK(currInode.direct_blocks[i]), SEEK_SET);
            
            while (remainingSize > 0) {
                read(img, &inode, sizeof(uint32_t));
                read(img, &length, sizeof(uint16_t));
                read(img, &name_length, sizeof(uint8_t));
                read(img, &file_type, sizeof(uint8_t));
                read(img, name, name_length);
                
                if (strncmp(name, dirName, dirNameLen) == 0) {
                    return inode;
                }
                remainingSize -= length;
                lseek(img, length - (sizeof(uint32_t) + sizeof(uint16_t) + sizeof(uint8_t) + sizeof(uint8_t) + name_length), SEEK_CUR);
            }
        }
    }
    return 0;
}






int main(int argc, char* argv[]) {

    if (std::string(argv[1]) == "dup") {

        img = open(argv[2], O_RDWR);

        lseek(img, EXT2_SUPER_BLOCK_POSITION, SEEK_SET);
        superBlock =  (struct ext2_super_block * )malloc(sizeof(struct ext2_super_block));
        read(img, superBlock, sizeof(struct ext2_super_block));

        // read necessary superBlock fields
        blockSize = EXT2_UNLOG(superBlock->log_block_size);
        inodesPerGroup = superBlock -> inodes_per_group;
        inodeSize = superBlock -> inode_size;


        numberOfGroups = (unsigned int)ceil((double)superBlock->inode_count / (double)inodesPerGroup);
        groupDescriptors = (struct ext2_block_group_descriptor * )malloc(sizeof(struct ext2_block_group_descriptor) * numberOfGroups);
        lseek(img, (unsigned int)OFFSET_BLOCK(ceil((double)(EXT2_BOOT_BLOCK_SIZE + EXT2_SUPER_BLOCK_SIZE) / (double)blockSize)), SEEK_SET);
        read(img, groupDescriptors, sizeof(struct ext2_block_group_descriptor) * numberOfGroups);


        // Find source inode number

        unsigned int source_inode_number = 0;
        if (isNumber(std::string(argv[3]))) {
            source_inode_number = strtoul(argv[3], NULL, 0);
        } 
        else {
            unsigned int temp_inode = 2;
            char *path;
            char *currentDirName;
            path = (char *)malloc(strlen(argv[3])+1);
            strcpy(path, argv[3]);
            for (currentDirName = strtok(path, "/"); currentDirName != NULL; currentDirName = strtok(NULL, "/")) {
                temp_inode = findInodeNumber(temp_inode, currentDirName);
            }
            free(path);

            source_inode_number = temp_inode;
        }

        
        // Find destination inode number

        char *destPath;
        destPath = (char *)malloc(strlen(argv[4])+1);
        char *destDirName;
        unsigned int dest_inode_number = 0;
        strcpy(destPath, argv[4]);

        destDirName = strtok(destPath, "/");



        if (isNumber(std::string(destDirName))) { // inode is given
            dest_inode_number = strtoul(destDirName, NULL, 0);
            destDirName = strtok(NULL, "/");
        }
        else { // abs path is given

            std::string s = std::string(argv[4]);
            char ch = '/';

            // find number of '/' characters
            int count = 0;
            for (int i = 0; (i = s.find(ch, i)) != std::string::npos; i++) {
                count++;
            }

            std::string realDir;
            char *currDir;
            currDir = strtok(argv[4],"/");
            count--;

            while (currDir != NULL && count > 0)
            {

              realDir.append("/");
              realDir.append(currDir);
              count--;
                
              currDir = strtok (NULL, "/");
                
            }

            unsigned int temp_inode = 2;
            char *path;
            char *currentDirName;
            path = (char *)malloc(strlen(realDir.c_str())+1);
            strcpy(path, realDir.c_str());
            for (currentDirName = strtok(path, "/"); currentDirName != NULL; currentDirName = strtok(NULL, "/")) {
                temp_inode = findInodeNumber(temp_inode, currentDirName);
            }
            free(path);


            dest_inode_number = temp_inode;
            destDirName = currDir; // target file name
        }


        dupTask(source_inode_number, dest_inode_number, destDirName);

        free(destPath); 
    }


    free(superBlock);
    free(groupDescriptors);
    close(img);

    
    return 0;
}



unsigned int realEntrySize(uint8_t name_len) {
    return (sizeof(uint32_t) + sizeof(uint16_t) + sizeof(uint8_t) + sizeof(uint8_t) + name_len);
}


unsigned int roundUp4(uint32_t real_len) {
    return (real_len + (4 - (real_len % 4)));
}




void dupTask(unsigned int source_inode_number, unsigned int dest_inode_number, char *destFileName) {

    // fetch source inode
    struct ext2_inode source_inode;
    readInode(source_inode_number, &source_inode);

    
    unsigned int first_data_block_address = groupDescriptors[0].inode_table + (unsigned int)ceil((double)(inodeSize * inodesPerGroup) / (double)blockSize);


    
    unsigned int new_inode_number = 0;
    bitmapDef *bitmap = (bitmapDef * )malloc(blockSize);
    lseek(img, OFFSET_BLOCK(groupDescriptors[0].inode_bitmap), SEEK_SET);
    read(img, bitmap, blockSize);

    

    for (int i = 0; i < numberOfGroups; i++) {
        
        lseek(img, OFFSET_BLOCK(groupDescriptors[i].inode_bitmap), SEEK_SET);
        read(img, bitmap, blockSize);
        if (groupDescriptors[i].free_inode_count > 0) {
            for (int j = 0; j < inodesPerGroup; j++) {
                if(isBitSet(j, bitmap) == false) { 
                    new_inode_number = inodesPerGroup*i+j+1;
                    if (new_inode_number < RESERVED_INODE_COUNT)
                        continue;
                    
                    setBit(j, bitmap);
                    
                    groupDescriptors[i].free_inode_count--;
                    superBlock -> free_inode_count--;

                    lseek(img, OFFSET_BLOCK(groupDescriptors[i].inode_bitmap), SEEK_SET);
                    write(img, bitmap, blockSize);

                    lseek(img, EXT2_SUPER_BLOCK_POSITION, SEEK_SET);
                    write(img, superBlock, sizeof(struct ext2_super_block));

                    lseek(img, (unsigned int)OFFSET_BLOCK(ceil((double)(EXT2_BOOT_BLOCK_SIZE + EXT2_SUPER_BLOCK_SIZE) / (double)blockSize)), SEEK_SET);
                    write(img, groupDescriptors, sizeof(struct ext2_block_group_descriptor) * numberOfGroups);

                    break;
                }
            }
        }
        if (new_inode_number != 0) {
            break;
        } 
    }


    std::cout << new_inode_number << std::endl;
    


    struct ext2_inode new_inode;

    new_inode.mode = source_inode.mode;
    new_inode.uid = source_inode.uid;
    new_inode.size = source_inode.size;

    std::time_t result = std::time(nullptr);


    new_inode.access_time = (uint32_t)result;
    new_inode.creation_time = (uint32_t)result;
    new_inode.modification_time = (uint32_t)result;
    new_inode.deletion_time = 0;
    new_inode.gid = source_inode.gid;
    new_inode.link_count = source_inode.link_count;
    new_inode.block_count_512 = source_inode.block_count_512;
    new_inode.flags = source_inode.flags;
    new_inode.reserved = source_inode.reserved;
    new_inode.direct_blocks[0] = source_inode.direct_blocks[0];
    new_inode.direct_blocks[1] = source_inode.direct_blocks[1];
    new_inode.direct_blocks[2] = source_inode.direct_blocks[2];
    new_inode.direct_blocks[3] = source_inode.direct_blocks[3];
    new_inode.direct_blocks[4] = source_inode.direct_blocks[4];
    new_inode.direct_blocks[5] = source_inode.direct_blocks[5];
    new_inode.direct_blocks[6] = source_inode.direct_blocks[6];
    new_inode.direct_blocks[7] = source_inode.direct_blocks[7];
    new_inode.direct_blocks[8] = source_inode.direct_blocks[8];
    new_inode.direct_blocks[9] = source_inode.direct_blocks[9];
    new_inode.direct_blocks[10] = source_inode.direct_blocks[10];
    new_inode.direct_blocks[11] = source_inode.direct_blocks[11];
    new_inode.single_indirect = 0;
    new_inode.double_indirect = 0;
    new_inode.triple_indirect = 0;


    std::vector<unsigned int> toBeUpdatedBlocks;


    for(int i=0;i<EXT2_NUM_DIRECT_BLOCKS;i++) {
        if (source_inode.direct_blocks[i] != 0) {
            toBeUpdatedBlocks.push_back(source_inode.direct_blocks[i]);
        }
    }
    

    // update reference counters for refmap

    unsigned int block_refmap_pos = groupDescriptors[iNodeBlockGroupNumber(source_inode_number)].block_refmap;

    refctr_t *referenceCounters = (refctr_t * )malloc(32*blockSize);

    lseek(img, OFFSET_BLOCK(block_refmap_pos), SEEK_SET);
    read(img, referenceCounters, 32*blockSize);

    for (auto it = toBeUpdatedBlocks.begin(); it != toBeUpdatedBlocks.end(); ++it) {

        if (blockSize == 1024) {
            referenceCounters[*it-1]++;
        } else {
            referenceCounters[*it]++;
        }
        
    }


    lseek(img, OFFSET_BLOCK(block_refmap_pos), SEEK_SET);
    write(img, referenceCounters, 32*blockSize);


    // write new inode to the inode table of corresponding group
    writeInode(new_inode_number, &new_inode);



    unsigned int destfile_name_len = strlen(destFileName);
    
    // preparing the entry to be added to destination inode.
    uint32_t inode = new_inode_number;
    uint16_t length = 0; // entry length
    uint8_t name_len = destfile_name_len;
    uint8_t file_type = 1; // assuming regular file
    char name[EXT2_MAX_NAME_LENGTH];

    strncpy(name, destFileName, destfile_name_len);


    // read destination inode
    struct ext2_inode dest_inode;
    readInode(dest_inode_number, &dest_inode);

    

    unsigned int remainingBlockSize = blockSize; // assuming for 1 block
    // temp dir_entry fields
    uint32_t temp_inode;
    uint16_t temp_length;
    uint8_t temp_name_len;
    uint32_t temp_real_len;

    char flag = 0;
    char flagAllocatedBlock = 0;
    int flagAllocatedI;
    
    // check allocated blocks of destination first
    for (int i = 0; i < EXT2_NUM_DIRECT_BLOCKS; i++) {
       
        if (dest_inode.direct_blocks[i] != 0) { 
            
            remainingBlockSize = blockSize;
            
            lseek(img, OFFSET_BLOCK(dest_inode.direct_blocks[i]), SEEK_SET);
            read(img, &temp_inode, sizeof(uint32_t));
            
            while (remainingBlockSize > 0) {
                if (temp_inode == 0) {
                    
                    read(img, &temp_length, sizeof(temp_length));
                    read(img, &temp_name_len, sizeof(temp_name_len));

                    remainingBlockSize -= temp_length;
                    if (remainingBlockSize == 0) {
                        flagAllocatedBlock = 1;
                        flagAllocatedI = i;
                        break;
                    }
                    if (temp_length >= ((8 + name_len + 3) & (~3))) { // holey case
                        length = ((8 + name_len + 3) & (~3));
                        lseek(img, -(sizeof(temp_inode) + sizeof(temp_length) + sizeof(temp_name_len)), SEEK_CUR);
                        write(img, &inode, sizeof(uint32_t));
                        write(img, &length, sizeof(uint16_t));
                        write(img, &name_len, sizeof(uint8_t));
                        write(img, &file_type, sizeof(uint8_t));
                        write(img, name, name_len);
                        flag = 1;

                        uint16_t remainingLength = temp_length - length;
                        if (remainingLength >= (sizeof(uint32_t) + sizeof(uint16_t))) {
                            lseek(img, length -(sizeof(uint32_t) + sizeof(uint16_t) + sizeof(uint8_t) + sizeof(uint8_t) + name_len), SEEK_CUR);
                            uint32_t zeroInode = 0;
                            write(img, &zeroInode, sizeof(uint32_t));
                            write(img, &remainingLength, sizeof(uint16_t));
                        }
                        break;
                    }

                    lseek(img, temp_length - (sizeof(temp_inode) + sizeof(temp_length) + sizeof(temp_name_len)), SEEK_CUR);
                    read(img, &temp_inode, sizeof(temp_inode));
                    continue;
                }

                read(img, &temp_length, sizeof(temp_length));
                read(img, &temp_name_len, sizeof(temp_name_len));
                temp_real_len = temp_name_len + 8;
                
                if ((roundUp4(temp_real_len) < temp_length) && (realEntrySize(name_len) <= (temp_length - roundUp4(temp_real_len)))) {
                    
                    temp_length = roundUp4(temp_real_len);
                    
                    lseek(img, -(sizeof(temp_length) + sizeof(temp_name_len)), SEEK_CUR);
                    write(img, &temp_length, sizeof(temp_length));
                    lseek(img, temp_length -(sizeof(temp_length) + sizeof(temp_inode)), SEEK_CUR);
                    
                    length = remainingBlockSize - temp_length;
                    
                    write(img, &inode, sizeof(uint32_t));
                    write(img, &length, sizeof(uint16_t));
                    write(img, &name_len, sizeof(uint8_t));
                    write(img, &file_type, sizeof(uint8_t));
                    write(img, name, name_len);
                    
                    flag = 1;
                    break;
                }
                remainingBlockSize -= temp_length;
                
                lseek(img, temp_length - (sizeof(temp_inode) + sizeof(temp_length) + sizeof(temp_name_len)), SEEK_CUR);
                read(img, &temp_inode, sizeof(temp_inode));
            }
            if (flag) {
                break;
            } 
        } 
    }



    if (flagAllocatedBlock) { // there is a pre-allocated empty block.
        
        remainingBlockSize = blockSize;
        lseek(img, OFFSET_BLOCK(dest_inode.direct_blocks[flagAllocatedI]), SEEK_SET);

        length = remainingBlockSize;

        write(img, &inode, sizeof(uint32_t));
        write(img, &length, sizeof(uint16_t));
        write(img, &name_len, sizeof(uint8_t));
        write(img, &file_type, sizeof(uint8_t));
        write(img, name, name_len);
        
        flag = 1;      
    }


    if (flag) {
        std::cout << "-1" << std::endl;
    }
    else { 
        unsigned int newlyAllocatedBlock = 0;
        for (int i = 0; i < EXT2_NUM_DIRECT_BLOCKS; ++i) {
            if (dest_inode.direct_blocks[i] == 0) { // check unallocated blocks this time
                
                for (int k = (dest_inode_number - 1) / inodesPerGroup; k < numberOfGroups; ++k) {
                    
                    first_data_block_address = groupDescriptors[k].inode_table + (unsigned int)ceil((double)(inodeSize * inodesPerGroup) / (double)blockSize);
                    lseek(img, OFFSET_BLOCK(groupDescriptors[k].block_bitmap), SEEK_SET);
                    read(img, bitmap, blockSize);
                    if (groupDescriptors[k].free_block_count > 0) {
                        for (int j = 0; j < blockSize * 8; ++j) {
                            if(isBitSet(j, bitmap) == false) {
                                
                                setBit(j, bitmap);

                                newlyAllocatedBlock = first_data_block_address + j - (first_data_block_address % superBlock -> blocks_per_group) + superBlock -> first_data_block;

                                groupDescriptors[k].free_block_count--;
                                superBlock -> free_block_count--;

                                lseek(img, OFFSET_BLOCK(groupDescriptors[k].block_bitmap), SEEK_SET);
                                write(img, bitmap, blockSize);

                                lseek(img, EXT2_SUPER_BLOCK_POSITION, SEEK_SET);
                                write(img, superBlock, sizeof(struct ext2_super_block));

                                lseek(img, (unsigned int)OFFSET_BLOCK(ceil((double)(EXT2_BOOT_BLOCK_SIZE + EXT2_SUPER_BLOCK_SIZE) / (double)blockSize)), SEEK_SET);
                                write(img, groupDescriptors, sizeof(struct ext2_block_group_descriptor) * numberOfGroups);

                                unsigned int block_refmap_pos = groupDescriptors[k].block_refmap;

                                refctr_t *referenceCounters = (refctr_t * )malloc(32*blockSize);

                                lseek(img, OFFSET_BLOCK(block_refmap_pos), SEEK_SET);
                                read(img, referenceCounters, 32*blockSize);

                                if (blockSize == 1024) {
                                    referenceCounters[newlyAllocatedBlock-1]++;
                                } else {
                                    referenceCounters[newlyAllocatedBlock]++;
                                }

                                lseek(img, OFFSET_BLOCK(block_refmap_pos), SEEK_SET);
                                write(img, referenceCounters, 32*blockSize);

                                free(referenceCounters);

                                break;
                            }
                        }
                    }
                    if (newlyAllocatedBlock) {
                        break;
                    } 
                }

                lseek(img, OFFSET_BLOCK(newlyAllocatedBlock), SEEK_SET);
                length = blockSize;

                write(img, &inode, sizeof(uint32_t));
                write(img, &length, sizeof(uint16_t));
                write(img, &name_len, sizeof(uint8_t));
                write(img, &file_type, sizeof(uint8_t));
                write(img, name, name_len);

                dest_inode.direct_blocks[i] = newlyAllocatedBlock;
                dest_inode.block_count_512 += (blockSize / 512);
                dest_inode.size += blockSize;

                writeInode(dest_inode_number, &dest_inode);
                std::cout << newlyAllocatedBlock << std::endl;
                break;
            } 
        }
    }


    free(bitmap);
    free(referenceCounters);

}