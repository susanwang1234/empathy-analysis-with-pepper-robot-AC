import argparse
import json
import os
from collections import OrderedDict
import torch
import csv
import util
from transformers import BertTokenizer, BertForQuestionAnswering


from transformers import AutoTokenizer, AutoModelWithLMHead

tokenizer2 = AutoTokenizer.from_pretrained("mrm8488/t5-base-finetuned-emotion")
model2 = AutoModelWithLMHead.from_pretrained("mrm8488/t5-base-finetuned-emotion")

checkpoint_path = os.path.join('./model/checkpoint')
model = BertForQuestionAnswering.from_pretrained(checkpoint_path)
tokenizer = BertTokenizer.from_pretrained('bert-large-uncased-whole-word-masking-finetuned-squad')

passage = ""

with open('./context-covid.txt', 'r') as f:
    passage = f.read().replace('\n','')
    

def get_emotion(text):
  input_ids = tokenizer2.encode(text + '</s>', return_tensors='pt')

  output = model2.generate(input_ids=input_ids, max_length=2)
  
  dec = [tokenizer2.decode(ids) for ids in output]
  label = dec[0]
  return label

def QA(question):
    # device = torch.device('cuda') if torch.cuda.is_available() else torch.device('cpu')
    device = "cuda:0"
    

    model.to(device)
    model.eval()
    # question = "what do you think?"

    # passage = "I am Cody."
    input_ids = tokenizer.encode(question, passage)
    tokens = tokenizer.convert_ids_to_tokens(input_ids)
    sep_index = input_ids.index(tokenizer.sep_token_id)
    num_seg_a = sep_index + 1
    num_seg_b = len(input_ids) - num_seg_a
    segment_ids = [0]*num_seg_a + [1]*num_seg_b
    assert len(segment_ids) == len(input_ids)
    start_scores, end_scores = model(torch.tensor([input_ids]).to(device), token_type_ids=torch.tensor([segment_ids]).to(device), return_dict=False)
    answer_start = torch.argmax(start_scores)
    answer_end = torch.argmax(end_scores)
    answer = ' '.join(tokens[answer_start:answer_end+1])
    return answer
